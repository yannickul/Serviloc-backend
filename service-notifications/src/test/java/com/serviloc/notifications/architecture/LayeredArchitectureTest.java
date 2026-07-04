package com.serviloc.notifications.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Vérifie le respect des règles de Clean Architecture / DDD pour service-notifications :
 * - le domaine ne dépend d'aucun framework (Spring, JPA...)
 * - les couches ne dépendent que vers l'intérieur (presentation/infrastructure -> application -> domain)
 */
class LayeredArchitectureTest {

    private final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.serviloc.notifications");

    @Test
    void domainShouldNotDependOnSpringFramework() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "com.google.firebase..");
        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnInfrastructureOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure..",
                        "..presentation..");
        rule.check(classes);
    }

    @Test
    void applicationShouldNotDependOnInfrastructureOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure..",
                        "..presentation..");
        rule.check(classes);
    }
}
