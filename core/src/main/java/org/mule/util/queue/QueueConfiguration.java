/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util.queue;

import org.mule.api.MuleContext;
import org.mule.api.MuleRuntimeException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.store.ListableObjectStore;
import org.mule.api.store.ObjectStore;
import org.mule.config.i18n.CoreMessages;

public class QueueConfiguration
{
    protected final int capacity;
    protected final ListableObjectStore objectStore;

    public QueueConfiguration(MuleContext context, int capacity, String storeName)
    {
        this.capacity = capacity;
        this.objectStore = context.getObjectStore(storeName);
        if (this.objectStore == null)
        {
            throw new MuleRuntimeException(CoreMessages.objectStoreNotFound(storeName));
        }
    }

    public QueueConfiguration(int capacity, ListableObjectStore objectStore)
    {
        this.capacity = capacity;
        this.objectStore = objectStore;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + capacity;
        result = prime * result + objectStore.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        QueueConfiguration other = (QueueConfiguration) obj;
        if (capacity != other.capacity)
        {
            return false;
        }
        if (!objectStore.equals(objectStore))
        {
            return false;
        }
        return true;
    }

    public boolean isPersistent()
    {
        return objectStore.isPersistent();
    }
}
