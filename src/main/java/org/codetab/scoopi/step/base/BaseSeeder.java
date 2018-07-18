package org.codetab.scoopi.step.base;

import org.codetab.scoopi.step.Step;

/**
 * <p>
 * Base seeder step.
 * @author Maithilish
 *
 */
public abstract class BaseSeeder extends Step {

    /**
     * <p>
     * do nothing.
     * @return false
     */
    @Override
    public boolean load() {
        return false;
    }

    /**
     * <p>
     * do nothing.
     * @return false
     */
    @Override
    public boolean process() {
        return false;
    }

    /**
     * <p>
     * do nothing.
     * @return false
     */
    @Override
    public boolean store() {
        return false;
    }

}
