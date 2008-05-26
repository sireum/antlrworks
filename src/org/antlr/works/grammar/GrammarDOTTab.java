package org.antlr.works.grammar;

import org.antlr.works.components.container.ComponentContainerGrammarMenu;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.grammar.decisiondfa.DecisionDFA;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.gview.GViewDelegate;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.utils.GDOTImporterDOT;
import org.antlr.xjlib.appkit.utils.XJAlert;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

public abstract class GrammarDOTTab extends EditorTab implements Runnable, GViewDelegate {

    protected ComponentEditorGrammar editor;

    protected JPanel panel;
    protected GView view;

    protected ElementRule rule;

    protected String tempInputFile;
    protected String tempOutputFile;

    protected String error;

    public GrammarDOTTab(ComponentEditorGrammar editor) {
        this.editor = editor;
    }

    public Container getContainer() {
        return panel;
    }

    public static final String dotInfo = "The 'dot' tool is used to render directed graph. It can be downloaded from www.graphviz.org.";

    public boolean launch() {
        if(AWPrefs.getDOTToolPath() == null) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the graph because the 'dot' tool path is not defined. The path can be set in the Preferences.\n"+dotInfo);
            return false;
        }
        if(!new File(AWPrefs.getDOTToolPath()).exists()) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the graph because the 'dot' tool does not exist at the specified path. Check the tool path in the Preferences.\n"+dotInfo);
            return false;
        }

        if(willLaunch()) {
            new Thread(this).start();
            editor.showProgress("Generating...", null);
            return true;
        } else
            return false;
    }

    protected boolean willLaunch() {
        return true;
    }

    protected boolean checkForCurrentRule() {
        ElementRule rule = editor.getCurrentRule();
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Error", "The cursor must be inside a rule");
            return false;
        }
        return true;
    }
    
    protected void createInterface(GElement graph) {
        panel = new JPanel(new BorderLayout());

        view = new CustomGView();
        view.setAutoAdjustSize(true);
        view.setRootElement(graph);
        view.setBackground(Color.white);
        view.setDrawBorder(false);
        view.setDelegate(this);
        
        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Zoom"));
        b.add(createZoomSlider());

        JScrollPane sp = new JScrollPane(view);
        sp.setWheelScrollingEnabled(true);

        panel.add(b, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
    }

    protected JSlider createZoomSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(1);
        slider.setMaximum(800);
        slider.setValue(100);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();

                view.setZoom((float)slider.getValue()/100);
                view.repaint();
            }
        });
        return slider;
    }

    public GElement generate() throws Exception {
        generateDOTFile();
        generatePlainTextFile();
        return new GDOTImporterDOT().generateGraph(tempOutputFile);
    }
    
    protected void generateDOTFile() throws Exception {
        String dot = getDOTString();
        if(dot != null) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempInputFile));
            bw.write(dot);
            bw.close();
        }
    }

    protected void generatePlainTextFile() throws Exception {
        String[] args = new String[] { Utils.quotePath(AWPrefs.getDOTToolPath()), "-Tdot", "-o",
                Utils.quotePath(tempOutputFile),
                Utils.quotePath(tempInputFile) };
        Process p = Runtime.getRuntime().exec(args);

        new StreamWatcher(p.getErrorStream(), "DecisionDFA").start();
        new StreamWatcher(p.getInputStream(), "DecisionDFA").start();

        p.waitFor();
    }

    public void willRun() {

    }

    public void run() {
        error = null;

        willRun();

        rule = editor.getCurrentRule();
        
        try {
            tempInputFile = File.createTempFile("GrammarDOTTab", ".in").getAbsolutePath();
            tempOutputFile = File.createTempFile("GrammarDOTTab", ".out").getAbsolutePath();

            createInterface(generate());
        } catch(Exception e) {
            e.printStackTrace();
            error = e.toString();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editor.hideProgress();
                if(error == null) {
                    editor.addTab(GrammarDOTTab.this);
                } else {
                    if(GrammarDOTTab.this instanceof TokensDFA)
                        XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the tokens DFA:\n"+error);
                    if(GrammarDOTTab.this instanceof DecisionDFA)
                        XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the DFA:\n"+error);
                    if(GrammarDOTTab.this instanceof RulesDependency)
                        XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the rule dependency graph:\n"+error);
                }
            }
        });

        new File(tempInputFile).delete();
        new File(tempOutputFile).delete();
    }

    public boolean canExportToEPS() {
        return true;
    }

    public boolean canExportToBitmap() {
        return true;
    }

    public boolean canExportToDOT() {
        return true;
    }

    public GView getExportableGView() {
        return view;
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public int getHorizontalMagnetics() {
        return 0;
    }

    public int getVerticalMagnetics() {
        return 0;
    }

    public void contextualHelp(GElement element) {
    }

    public void changeOccured() {
    }

    public void viewSizeDidChange() {
        // Let the JScrollPane know that the dfaView size may have changed
        view.revalidate();        
    }

    protected class CustomGView extends GView {

        public JPopupMenu getContextualMenu(GElement element) {
            ContextualMenuFactory factory = editor.createContextualMenuFactory();
            factory.addItem(ComponentContainerGrammarMenu.MI_EXPORT_AS_EPS);
            factory.addItem(ComponentContainerGrammarMenu.MI_EXPORT_AS_IMAGE);
            factory.addItem(ComponentContainerGrammarMenu.MI_EXPORT_AS_DOT);
            return factory.menu;
        }

    }

    protected class StreamWatcher extends Thread {

        InputStream is;
        String type;

        public StreamWatcher(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ( (line = br.readLine()) != null)
                    editor.console.println(type + ":" + line);
            } catch (IOException e) {
                editor.console.println(e);
            }
        }
    }

}
