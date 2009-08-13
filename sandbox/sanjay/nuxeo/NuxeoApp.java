/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.collectionspace.services.nuxeo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.nuxeo.common.Environment;
import org.nuxeo.osgi.BundleFile;
import org.nuxeo.osgi.BundleImpl;
import org.nuxeo.osgi.DirectoryBundleFile;
import org.nuxeo.osgi.JarBundleFile;
import org.nuxeo.osgi.OSGiAdapter;
import org.osgi.framework.BundleException;

/**
 * Nuxeo Runtime launcher.
 *
 * This launcher assume all bundles are already on the classpath.
 *
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class NuxeoApp {

    protected OSGiAdapter osgi;
    protected final ClassLoader loader;
    protected final Environment env;

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl == null ? NuxeoApp.class.getClassLoader() : cl;
    }

    public NuxeoApp() {
        this (new File("."), getDefaultClassLoader());
    }

    public NuxeoApp(File home) {
      this (home, getDefaultClassLoader());
    }

    public NuxeoApp(File home, ClassLoader loader) {
        this.loader = loader;
        env = new Environment(home);
    }

    public Environment getEnvironment() {
        return this.env;
    }

    public synchronized void deployBundles(Collection<File> files) throws BundleException, IOException {
        if (!isStarted()) {
            throw new IllegalStateException("Framework not started");
        }
        for (File file : files) {
            deployBundle(file);
        }
    }

    public synchronized void deployBundle(File file) throws BundleException, IOException  {
        if (!isStarted()) {
            throw new IllegalStateException("Framework not started");
        }
        BundleFile bf = file.isDirectory() ? new DirectoryBundleFile(file) : new JarBundleFile(file);
        osgi.install(new BundleImpl(osgi, bf, loader));
        System.out.println(">>>NuxeoApp:deployed bundle: " + file.getName());
    }

    public synchronized void start() {
        if (osgi != null) {
            //throw new IllegalStateException("Nuxeo Runtime already started");
            //sd: why throw exception?
            return;
        }
        osgi = new OSGiAdapter(env.getHome(), env.getData(), env.getProperties());
    }

    public synchronized boolean isStarted() {
        return osgi != null;
    }

    public synchronized OSGiAdapter getOsgi() {
        return osgi;
    }


    public synchronized void shutdown() throws IOException {
        if (osgi == null) {
            throw new IllegalStateException("Nuxeo Runtime not started");
        }
        osgi.shutdown();
        osgi = null;
    }

    public static Collection<File> getBundleFiles(File baseDir, String bundles, String delim) throws IOException {
        LinkedHashSet<File> result = new LinkedHashSet<File>();
        StringTokenizer tokenizer = new StringTokenizer(bundles, delim == null ? " \t\n\r\f" : delim);
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            List<File> files = expandFiles(baseDir, tok);
            for (File file : files) {
                result.add(file.getCanonicalFile());
            }
        }
        return result;
    }

    public static List<File> expandFiles(File baseDir, String line) {
        int p = line.lastIndexOf("/");
        String fileName = null;
        if (p > -1) {
            fileName = line.substring(p+1);
            baseDir = new File(baseDir, line.substring(0, p));
        } else {
            fileName = line;
        }
        p = fileName.indexOf("*");
        if (p == -1) {
            return Collections.singletonList(new File(baseDir, fileName));
        } else if (fileName.length() == 0) {
            return Arrays.asList(baseDir.listFiles());
        } else if (p == 0) {
            String suffix= fileName.substring(p+1);
            ArrayList<File> result = new ArrayList<File>();
            for (String name : baseDir.list()) {
                if (name.endsWith(suffix)) {
                    result.add(new File(baseDir, name));
                }
            }
            return result;
        } else if (p == fileName.length()-1) {
            String prefix= fileName.substring(0, p);
            ArrayList<File> result = new ArrayList<File>();
            for (String name : baseDir.list()) {
                if (name.startsWith(prefix)) {
                    result.add(new File(baseDir, name));
                }
            }
            return result;
        } else {
            String prefix= fileName.substring(0, p);
            String suffix= fileName.substring(p+1);
            ArrayList<File> result = new ArrayList<File>();
            for (String name : baseDir.list()) {
                if (name.startsWith(prefix) && name.endsWith(suffix)) {
                    result.add(new File(baseDir, name));
                }
            }
            return result;
        }
    }

}
