package org.antlr.works.editor;

import org.antlr.works.ate.breakpoint.ATEBreakpointEntity;
import org.antlr.works.ate.breakpoint.ATEBreakpointManager;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.syntax.element.ElementRule;

import java.util.ArrayList;
import java.util.List;
/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
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

public class EditorBreakpointManager extends ATEBreakpointManager {

    protected ComponentEditorGrammar editor;

    public EditorBreakpointManager(ComponentEditorGrammar editor) {
        super(editor.textEditor);
        this.editor = editor;
    }

    public List<? extends ATEBreakpointEntity> getBreakpointEntities() {
        List<ElementRule> entities = new ArrayList<ElementRule>();
        List<ElementRule> rules = editor.parserEngine.getRules();
        if(rules != null)
            entities.addAll(rules);
        return entities;
    }

    @Override
    public void close() {
        super.close();
        editor = null;
    }
}
