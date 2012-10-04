package com.google.code.ssm.jmemcached.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * 
 * @goal stop
 * 
 * @phase post-integration-test
 */
public class JmemcachedStopMojo extends AbstractJmemcachedMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        stop();
    }

}
