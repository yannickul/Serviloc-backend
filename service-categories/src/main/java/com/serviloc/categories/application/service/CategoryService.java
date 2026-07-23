package com.serviloc.categories.application.service;

import com.serviloc.categories.application.dto.CategoryDeletedResponse;
import com.serviloc.categories.application.dto.CategoryIncrementResponse;
import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.dto.CategoryUpsertRequest;
import com.serviloc.categories.application.dto.ClientCategoryResponse;
import com.serviloc.categories.application.mapper.CategoryDtoMapper;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
import com.serviloc.categories.domain.exception.DuplicateCategoryLabelException;
import com.serviloc.categories.domain.model.BudgetRange;
import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.IconKey;
import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.ServiceCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service applicatif : orchestre les cas d'usage du référentiel des catégories.
 * Les frontières transactionnelles et le cache Redis sont gérés ici (couche application),
 * jamais dans le domaine.
 */
@Service
@Transactional
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    public static final String CATEGORIES_CACHE = "categories";
    public static final String CLIENT_LIST_CACHE_KEY = "'client-list'";

    private final ServiceCategoryRepository repository;

    public CategoryService(ServiceCategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * GET /client/categories — vue publique, mise en cache Redis (TTL 1h configuré globalement).
     * Ne contient pas demandCount/percentageShare (voir ClientCategoryResponse).
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CATEGORIES_CACHE, key = CLIENT_LIST_CACHE_KEY)
    public List<ClientCategoryResponse> listForClient() {
        log.debug("Cache miss categories:client-list — lecture depuis la base");
        return repository.findAll().stream()
                .map(CategoryDtoMapper::toClientResponse)
                .toList();
    }

    /**
     * GET /admin/categories — liste complète avec statistiques, toujours fraîche (pas de cache).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listForAdmin() {
        return listAllWithStats();
    }

    /**
     * GET /internal/categories — liste complète sans cache, pour Service Missions.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listForInternal() {
        return listAllWithStats();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(String rawId) {
        ServiceCategory category = repository.findById(CategoryId.of(rawId))
                .orElseThrow(() -> new CategoryNotFoundException(rawId));
        return CategoryDtoMapper.toResponse(category, repository.totalDemandCount());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getByLabel(String label) {
        ServiceCategory category = repository.findByLabelIgnoreCase(label)
                .orElseThrow(() -> CategoryNotFoundException.byLabel(label));
        return CategoryDtoMapper.toResponse(category, repository.totalDemandCount());
    }

    /**
     * GET /internal/categories/stats — mêmes données que la liste, exposées séparément
     * pour Service Missions (admin/stats). Forme inchangée (id/label/... + demandCount/
     * percentageShare) : le remapping vers { name, percentage, color } pour
     * admin/dashboard.popularCategories est fait côté Gateway.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getStats() {
        return listAllWithStats();
    }

    @CacheEvict(cacheNames = CATEGORIES_CACHE, key = CLIENT_LIST_CACHE_KEY)
    public CategoryResponse create(CategoryUpsertRequest request) {
        if (repository.existsByLabelIgnoreCase(request.label())) {
            throw new DuplicateCategoryLabelException(request.label());
        }
        ServiceCategory category = ServiceCategory.create(
                request.label(),
                IconKey.fromWireFormat(request.iconKey()),
                request.description(),
                request.color(),
                toBudgetRange(request.budgetRange()));
        ServiceCategory saved = repository.save(category);
        log.info("Catégorie créée : {}", saved.getId());
        return CategoryDtoMapper.toResponse(saved, repository.totalDemandCount());
    }

    @CacheEvict(cacheNames = CATEGORIES_CACHE, key = CLIENT_LIST_CACHE_KEY)
    public CategoryResponse update(String rawId, CategoryUpsertRequest request) {
        CategoryId id = CategoryId.of(rawId);
        ServiceCategory category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(rawId));

        repository.findByLabelIgnoreCase(request.label())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new DuplicateCategoryLabelException(request.label());
                });

        category.rename(
                request.label(),
                IconKey.fromWireFormat(request.iconKey()),
                request.description(),
                request.color(),
                toBudgetRange(request.budgetRange()));
        ServiceCategory saved = repository.save(category);
        log.info("Catégorie mise à jour : {}", saved.getId());
        return CategoryDtoMapper.toResponse(saved, repository.totalDemandCount());
    }

    @CacheEvict(cacheNames = CATEGORIES_CACHE, key = CLIENT_LIST_CACHE_KEY)
    public CategoryDeletedResponse delete(String rawId) {
        CategoryId id = CategoryId.of(rawId);
        if (!repository.existsById(id)) {
            throw new CategoryNotFoundException(rawId);
        }
        repository.deleteById(id);
        log.info("Catégorie supprimée : {}", rawId);
        return new CategoryDeletedResponse(rawId, true);
    }

    /**
     * PUT /internal/categories/{id}/increment — appelé par le consumer RabbitMQ (demand.published)
     * ou directement en interne. N'invalide volontairement pas le cache client (staleness
     * acceptable pendant 1h maximum sur ce référentiel peu volatile).
     */
    public CategoryIncrementResponse incrementDemandCount(String rawId) {
        CategoryId id = CategoryId.of(rawId);
        ServiceCategory category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(rawId));
        category.incrementDemandCount();
        ServiceCategory saved = repository.save(category);
        log.debug("demandCount incrémenté pour {} -> {}", saved.getId(), saved.getDemandCount());
        return new CategoryIncrementResponse(saved.getId().value(), saved.getDemandCount());
    }

    private List<CategoryResponse> listAllWithStats() {
        List<ServiceCategory> categories = repository.findAll();
        long total = repository.totalDemandCount();
        return categories.stream()
                .map(c -> CategoryDtoMapper.toResponse(c, total))
                .toList();
    }

    private static BudgetRange toBudgetRange(CategoryUpsertRequest.BudgetRangeRequest request) {
        return new BudgetRange(request.min(), request.max());
    }
}
