package com.serviloc.mission.domain.exception;

public class QuoteNotFoundException extends RuntimeException{
    public QuoteNotFoundException( String quoteId){
        super("le devis" + quoteId + " n'existe pas");
    }
}