package io.breen.socrates.controller;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.constructor.MissingResourceException;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.hooks.HookManager;
import io.breen.socrates.immutable.hooks.triggers.Hook;
import io.breen.socrates.immutable.submission.*;
import io.breen.socrates.view.DetailOptionPane;
import io.breen.socrates.view.setup.SetupView;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class SetupController {

    private static Logger logger = Logger.getLogger(SetupController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;

    private MainController main;
    private SetupView view;

    public SetupController(MainController main) {
        view = new SetupView();
        this.main = main;

        view.addOpenCriteriaButtonActionListener(
                e -> {
                    Path path = view.chooseCriteriaFile();
                    if (path != null) {
                        try {
                            criteria = Criteria.loadFromPath(path);
                        } catch (IOException | InvalidCriteriaException |
                                MissingResourceException x) {
                            DetailOptionPane.showMessageDialog(
                                    view,
                                    "There was an error opening the criteria file you " +
                                            "selected.",
                                    "Error Opening Criteria",
                                    JOptionPane.ERROR_MESSAGE,
                                    x.toString()
                            );
                            return;
                        }

                        // criteria was successfully loaded
                        logger.info("criteria was successfully loaded");
                        HookManager.runHook(Hook.AFTER_CRITERIA_LOAD);

                        if (submissions == null) view.showSubmissionsCard();
                        else transferToMain();
                    }
                }
        );

        view.addSubmissionsButtonActionListener(
                event -> {
                    List<Path> ps = view.chooseSubmissions();
                    if (ps != null) {
                        Map<Path, Exception> errors = new HashMap<>();
                        submissions = new ArrayList<>(ps.size());
                        for (Path p : ps) {
                            try {
                                submissions.add(Submission.fromDirectory(p));
                            } catch (IOException x) {
                                errors.put(p, x);
                                logger.warning("IOE thrown when adding submission: " + x);
                            } catch (ReceiptFormatException x) {
                                errors.put(p, x);
                                logger.warning("RFE thrown when adding submission: " + x);
                            } catch (AlreadyGradedException x) {
                                errors.put(p, x);
                                logger.info("AGE thrown when adding submission: " + x);
                            }
                        }

                        int numErrors = errors.size();
                        int numAdded = submissions.size();
                        if (numErrors > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<Path, Exception> e : errors.entrySet())
                                sb.append(e.getKey() + ": " + e.getValue() + "\n");

                            String msg = "There was a problem opening " + numErrors + "" +
                                    " submission" + (numErrors == 1 ? "" : "s") + ".";
                            if (numAdded > 0) {
                                msg += " The remaining " + numAdded + " submission" +
                                        (numAdded == 1 ? " is" : "s are") + " available" +
                                        " to grade.";
                            }
                            String title = (numErrors == 1 ? "Error" : "Errors") + " " +
                                    "Opening Submissions";

                            DetailOptionPane.showMessageDialog(
                                    view, msg, title, JOptionPane.INFORMATION_MESSAGE, sb.toString()
                            );
                        }

                        if (numAdded > 0) {
                            HookManager.runHook(Hook.BEFORE_GRADING);
                            transferToMain();
                        } else {
                            logger.warning("no submissions could be added");
                        }
                    }
                }
        );
    }

    public void start(Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        if (criteria == null) {
            view.showCriteriaCard();
            view.setVisible(true);
        } else if (submissions == null) {
            // we can skip the "Choose a criteria file" step
            HookManager.runHook(Hook.AFTER_CRITERIA_LOAD);
            view.showSubmissionsCard();
            view.setVisible(true);
        } else {
            // we can skip the setup entirely
            HookManager.runHook(Hook.AFTER_CRITERIA_LOAD);
            HookManager.runHook(Hook.BEFORE_GRADING);
            main.start(criteria, submissions);
        }
    }

    public void transferToMain() {
        view.setVisible(false);
        view.dispose();
        main.start(criteria, submissions);
    }
}
