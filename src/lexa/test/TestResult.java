/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexa.test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class TestResult
{
    private final String name;
    private final Boolean complete;
    private final Boolean pass;
    private final Throwable exception;
    private final List<TestResult> children;

    /**
     *
     * @param name
     */
    public TestResult(String name)
    {
        this.name = name;
        this.complete = null;
        this.pass = null;
        this.exception = null;
        this.children = new ArrayList();
    }

    /**
     *
     * @param name
     * @param pass
     */
    public TestResult(String name, boolean pass)
    {
        this(name, true, pass, null);
    }

    /**
     *
     * @param name
     * @param complete
     * @param pass
     * @param exception
     */
    public TestResult(String name, boolean complete, boolean pass, Throwable exception)
    {
        this.name = name;
        // The three items MUST be consistant:
        this.pass = pass && complete && (exception == null);
        this.complete = complete && (exception == null);;
        this.exception = exception;
        this.children = null;
    }

    /**
     *
     * @return
     */
    public boolean isParent()
    {
        return (this.children != null);
    }

    /**
     *
     * @param result
     * @return
     */
    public boolean addResultIfFailed(TestResult result)
    {
        if (!result.passed())
        {
            this.addResult(result);
            return true;
        }
        return false;
    }

    /**
     *
     * @param result
     */
    public void addResult(TestResult result)
    {
        if (!this.isParent())
            throw new IllegalArgumentException("Can only add children to a parent");
        if (result.isParent() && result.children.size() == 1)
        {
            // ignore parent container with a single test
            this.addResult(result.children.get(0));
            return;
        }
        this.children.add(result);
    }

    /**
     *
     * @return
     */
    public String getName()
    {
        return this.name;
    }

    /**
     *
     * @return
     */
    public Throwable getException()
    {
        return this.exception;
    }

    /**
     *
     * @return
     */
    public int getCompleteCount()
    {
        if (!this.isParent())
            return this.complete ? 1 : 0;
        int count=0;
        for (TestResult result : this.children)
            count+= result.getCompleteCount();
        return count;
    }

    /**
     *
     * @return
     */
    public int getPassCount()
    {
        if (!this.isParent())
            return this.pass ? 1 : 0;
        int count=0;
        for (TestResult result : this.children)
            count+= result.getPassCount();
        return count;
    }

    /**
     *
     * @return
     */
    public int getTestCount()
    {
        if (!this.isParent())
            return 1;
        int count=0;
        for (TestResult result : this.children)
            count+= result.getTestCount();
        return count;
    }

    /**
     *
     * @return
     */
    public String getReport()
    {
        return this.getReport(true, true);
    }

    /**
     *
     * @param details
     * @param exceptions
     * @return
     */
    public String getReport(boolean details, boolean exceptions)
    {
        StringBuilder report = new StringBuilder();
        this.buildReport(report, true, details, exceptions);

        return report.toString();
    }

    private void buildReport(StringBuilder report,boolean topLevel, boolean details, boolean exceptions)
    {
        if (topLevel)
        {
            report.append(this.getName()).append(":-\n")
                    .append("Tests     ").append(this.getTestCount()).append('\n')
                    .append("Completed ").append(this.getCompleteCount()).append('\n')
                    .append("Passed    ").append(this.getPassCount()).append('\n');
            if (details || !this.passed())
            {
                report
                    .append("\nName                                             Complete     Pass\n")
                    .append("================================================== ======== ========\n");
            }
        }

        if (this.isParent() && (exceptions || details))
        {
            for (TestResult result : this.children)
                result.buildReport(report, false, details, exceptions);
        }

        if (details || !this.passed())
        {
            if (this.isParent())
            {
                report
                        .append(String.format("%8d", this.getTestCount()))
                        .append(" ")
                        .append(String.format("%1$-41s", this.getName()))
                        .append(String.format("%9d", this.getCompleteCount()))
                        .append(String.format("%9d", this.getPassCount()))
                        .append('\n');
            }
            else
            {
                report
                        .append(String.format("%1$-50s", this.getName()))
                        .append(this.completed() ? "      YES" : "       NO")
                        .append(this.passed() ? "      YES" : "       NO")
                        .append('\n');
            }
        }
        if (exceptions)
        {
            Throwable ex =  this.getException();
            if (ex != null)
            {
                report.append("  >> ")
                        .append(this.getException())
                        .append('\n')
                        .append("  >> Cause:\n");
                for ( StackTraceElement ste : ex.getStackTrace())
                {
                    report.append("  >> ")
                            .append(ste.getClassName())
                            .append('.')
                            .append(ste.getMethodName())
                            .append(" at ")
                            .append(ste.getLineNumber())
                            .append('\n');
                    if (ste.getClassName().indexOf("lexa.test.")>-1)
                            break;
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean passed()
    {
        if (!this.isParent())
            return this.pass;
        for (TestResult result : this.children)
            if (!result.passed())
                return false;
        return true;
    }

    /**
     *
     * @return
     */
    public boolean completed()
    {
        if (!this.isParent())
            return this.complete;
        for (TestResult result : this.children)
            if (!result.completed())
                return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "TestResult{" + name +
                ", complete=" + complete + ", pass=" + pass + ", exception=" + exception + '}';
    }

}
