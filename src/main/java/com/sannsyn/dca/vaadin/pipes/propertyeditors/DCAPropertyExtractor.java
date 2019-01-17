package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import java.util.Optional;

/**
 * An extractor interface that will try extract value from String to any types.
 * Created by jobaer on 3/23/17.
 */
public interface DCAPropertyExtractor {
    Optional<Object> extract(Object value);
}
