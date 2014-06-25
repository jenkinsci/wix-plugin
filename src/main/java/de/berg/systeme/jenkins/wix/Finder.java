/*
 * This file is part of wix-plugin-jenkins.
 * 
 * Copyright (C) 2014 Berg Systeme
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.berg.systeme.jenkins.wix;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Bjoern.Berg
 */
public class Finder {
    private File target;
    final private List<File> results = new LinkedList<File>();
    
    /**
     * Constructor
     * @param f file to find.
     */
    public Finder(File f) {
        target = f;
    }
    
    public Finder(String filename) {
        this(new File(filename));
    }
    
    public void find(File file) {
        if ( file.getName().compareToIgnoreCase(target.getName()) == 0) {
            results.add(new File(file.getPath()));
            System.out.println("Found: " + file);
        }
    }
    
    public void walkFileTree(File startingDir) {
        try {
            if (startingDir != null && 
                startingDir.exists() && 
                startingDir.canRead()) {
                for (File f : startingDir.listFiles()) {
                    if (f.isDirectory()) {
                        System.out.println("Searching in: " + f.getAbsolutePath());
                        walkFileTree(f);
                    } else {
                        find(f);
                    }
                }
            } else {
                System.err.println("Not existent: " + startingDir);
            }
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
    
    public void walkFileTree(File[] directories) {
        for (File dir : directories) {
            walkFileTree(dir);
        }
    }
    
    public File[] getResults() {
        return results.toArray(new File[results.size()]);
    }
    
    public static void main(String[] args) {
        File[] programFiles = new File[2];
        programFiles[0] = new File(System.getenv("ProgramFiles"));
        programFiles[1] = new File(System.getenv("ProgramFiles(x86)"));
        
        Finder finder = new Finder(Wix.COMPILER);
        //finder.walkFileTree(new File("C:\\Program Files (x86)"));
        
        finder.walkFileTree(programFiles);
    }
}
