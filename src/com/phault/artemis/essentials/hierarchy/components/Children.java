package com.phault.artemis.essentials.hierarchy.components;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.LinkPolicy;
import com.artemis.utils.IntBag;

/**
 * Created by Casper on 22-07-2016.
 */
public class Children extends PooledComponent {
    @EntityId @LinkPolicy(LinkPolicy.Policy.CHECK_SOURCE)
    public IntBag targets = new IntBag(1);

    @Override
    protected void reset() {
        targets.setSize(0);
    }
}
