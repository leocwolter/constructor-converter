/*
 * Copyright (C) 2013 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. January 2013 by Leonardo Wolter & Francisco Sokol & Guilherme Silveira
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;


/**
 * <p> ParanamerParser is an adapter for using Paranamer library optionally</p>
 *
 * @author Leonardo Wolter
 * @author Francisco Sokol
 * @author Guilherme Silveira
 */
public class ParanamerParser {
    
    @SuppressWarnings("rawtypes")
	public String[] paramsFor(Constructor c) {
        Paranamer paranamer = new CachingParanamer(new AdaptiveParanamer());
        return paranamer.lookupParameterNames(c, true);
    }

}
