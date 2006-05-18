/*
 * Cascade.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.domain;

/**
 * Mark the specified class as one to which cascade behaviour is to apply. By
 * turning cascade on (particularly for the Collection classes included in the
 * array above) we create the magic that any time one of them is
 * {activated,updated} it will start a new {activate,update} through depth.
 * 
 * @author Andrew Cowie
 */
public interface Cascade
{

}
