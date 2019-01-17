package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.service.Status;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;

import java.util.Map;

/**
 * Handler class for the create/edit form of pipes.
 * <p>
 * Created by jobaer on 6/21/16.
 */
class DCAPipeEditFormHandler {
    private DCAPipesService recService;
    private Runnable refreshAction;

    DCAPipeEditFormHandler(DCAUser loggedInUser, Runnable refreshAction) {
        recService = new DCAPipesService(loggedInUser);
        this.refreshAction = refreshAction;
    }

    Pair<Status, String> createOrUpdatePipe(Map<String, Object> formValues, String className,
                                            Map<String, Object> externalPipeData) {
        Pair<Status, String> updatePipe = recService.createUpdatePipe(formValues, className);
        Pair<Status, String> updateExternalDataMessage = recService.createUpdateExternalPipeData(externalPipeData).toBlocking().first();
        refreshAction.run();

        Status successOrFailure = updatePipe.getLeft().equals(Status.SUCCESS)
                && updateExternalDataMessage.getLeft().equals(Status.SUCCESS) ? Status.SUCCESS : Status.FAILURE;
        Pair<Status, String> messagePair = new ImmutablePair<>(successOrFailure, String.format("%s-%s",
                updatePipe.getRight(), updateExternalDataMessage.getRight()));

        return messagePair;
    }
}
