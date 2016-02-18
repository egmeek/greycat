package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.ManualChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.press.PressHeapChunkSpace;
import org.kevoree.modeling.memory.space.impl.PhantomQueueChunkSpaceManager;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.scheduler.KScheduler;

public class PressHeapMemoryStrategy implements KMemoryStrategy {

    private int _maxEntries;

    public PressHeapMemoryStrategy(int maxEntries) {
        this._maxEntries = maxEntries;
    }

    @Override
    public KChunkSpace newSpace() {
        return new PressHeapChunkSpace(_maxEntries);
    }

    /**
     * @native ts
     * return new org.kevoree.modeling.memory.space.impl.NoopChunkSpaceManager(p_space);
     */
    @Override
    public KChunkSpaceManager newSpaceManager(KChunkSpace p_space, KScheduler p_scheduler) {
        return new PhantomQueueChunkSpaceManager(p_space, p_scheduler);
        //return new ManualChunkSpaceManager(p_space);
    }

}
