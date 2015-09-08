package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.PostConstructionAction;
import io.breen.socrates.immutable.Verifiable;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.any.LateSubmissionTest;

import java.util.*;

/**
 * Class representing an expected file specified by the criteria. Instances of non-abstract
 * subclasses of this class are immutable, and are created when a Criteria object is created.
 *
 * @see io.breen.socrates.immutable.criteria.Criteria
 */
public abstract class File implements Verifiable, PostConstructionAction {

    /**
     * The relative path from the root of any student's submission directory specifying where the
     * expected file can be found.
     */
    public String path;

    /**
     * The number of points that this file contributes to the total value of the assignment being
     * graded.
     */
    public double pointValue;

    /**
     * The content type value for this file. This may not be a MIME type; it should be whatever is
     * specified in the JSyntaxPane libraries in order for syntax highlighting to work.
     */
    public String contentType;

    public Map<Date, Double> dueDates;

    /**
     * This file's "test tree" root. The root is a TestGroup object whose maxValue field is equal to
     * this file's point value. (This is to prevent tests deducting more points than are allocated
     * to this file.)
     *
     * @see TestGroup
     */
    public TestGroup testRoot;

    public List<Object> tests = new LinkedList<>();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public File() {}

    public File(String path, double pointValue, Map<Date, Double> dueDates, List<Object> tests) {
        this.path = path;
        this.pointValue = pointValue;
        this.dueDates = dueDates;
        this.tests = tests;
        afterConstruction();
    }

    @Override
    public void afterConstruction() {
        testRoot = createTestRoot();
    }

    @Override
    public void verify() {
        if (path == null || contentType == null || tests == null)
            throw new IllegalArgumentException();
    }

    /**
     * This method creates this file's test "root". The root is a test group that limits the maximum
     * total value of the descendant tests to the total value of the file. This method may also
     * create a test group of LateSubmissionTest objects, if the criteria file specifies due dates
     * for this file. (That test group would be a child of the root.)
     */
    protected TestGroup createTestRoot() {
        if (dueDates != null) {
            SortedMap<Date, Double> sorted = new TreeMap<>(Collections.reverseOrder());
            sorted.putAll(dueDates);

            List<Object> lateTests = new ArrayList<>(sorted.size());

            /*
             * It is *very* important that we process these due dates latest-to-earliest,
             * and that we add them to the list in that order. This will ensure that the
             * highest-valued deduction is chosen first. In the case that there are many
             * due dates specifying different late periods, we will want to take the
             * deduction corresponding to the "latest" cutoff timestamp first.
             */
            for (Map.Entry<Date, Double> entry : sorted.entrySet()) {
                LateSubmissionTest lst = new LateSubmissionTest(entry.getValue(), entry.getKey());
                lateTests.add(lst);
            }

            TestGroup lateGroup = new TestGroup(lateTests, 1, 0.0);

            /*
             * Here we add the late tests before any of the other tests specified from
             * the criteria file. This just makes sense, since we want any late
             * deductions to be taken first, and therefore appear first in the grade
             * report.
             */
            tests.add(0, lateGroup);
        }

        return new TestGroup(tests, 0, pointValue);
    }

    public String toString() {
        return this.getClass().toString() + "(" +
                "path=" + path + ", " +
                "pointValue=" + pointValue + ", " +
                "tests=" + tests +
                ")";
    }

    /**
     * Returns the human-readable, user-friendly string representing the type of the file. This is
     * used by the GUI.
     */
    public abstract String getFileTypeName();
}
