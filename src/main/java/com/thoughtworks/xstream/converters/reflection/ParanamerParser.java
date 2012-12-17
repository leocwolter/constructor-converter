package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;

import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ParanamerParser {
    
    public String[] paramsFor(Constructor c) {
        Paranamer paranamer = new CachingParanamer();
        return paranamer.lookupParameterNames(c, true);
    }

}
