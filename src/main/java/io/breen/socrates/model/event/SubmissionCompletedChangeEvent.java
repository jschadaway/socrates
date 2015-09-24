package io.breen.socrates.model.event;

import io.breen.socrates.model.wrapper.SubmissionWrapperNode;
import io.breen.socrates.util.ObservableChangedEvent;

/**
 * The event that is generated by a SubmissionWrapperNode when all of its children that are
 * SubmittedFileWrapperNodes become completed (causing the Submission to be complete), or when at
 * least one child becomes not complete from being complete (causing the Submission to be not
 * complete).
 */
public class SubmissionCompletedChangeEvent extends ObservableChangedEvent<SubmissionWrapperNode> {

    public final boolean isNowComplete;

    public SubmissionCompletedChangeEvent(SubmissionWrapperNode source, boolean isNowComplete) {
        super(source);
        this.isNowComplete = isNowComplete;
    }

    @Override
    public String toString() {
        return "SubmissionCompletedChangeEvent(source=" + source + ", isNowComplete=" +
                isNowComplete + ")";
    }
}
