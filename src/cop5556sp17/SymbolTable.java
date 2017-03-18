package cop5556sp17;


import cop5556sp17.AST.Dec;

import java.util.*;


public class SymbolTable {

    int current_scope, next_scope;


    Map<String, HashMap<Integer, Dec>> entries = new HashMap<String, HashMap<Integer, Dec>>();
    Stack<Integer> scopeStack = new Stack<Integer>();

    /**
     * to be called when block entered
     */
    public void enterScope() {
        current_scope = next_scope++;
        scopeStack.push(current_scope);
    }


    /**
     * leaves scope
     */
    public void leaveScope() {
        if (scopeStack.size() > 0) {
            scopeStack.pop();
        }
    }

    public boolean insert(String ident, Dec dec) {
        Map entryMap = entries.get(ident);
        if (entryMap != null) {
            Dec temp = (Dec) entryMap.get(current_scope);
            if (temp != null) {
                return false;
            }
            entryMap.put(current_scope, dec);
        } else {
            entryMap.put(current_scope, dec);
        }
        entries.put(ident, (HashMap<Integer, Dec>) entryMap);
        return true;

    }

    public Dec lookup(String ident) {

        HashMap entryMap = entries.get(ident);
        if (entryMap == null) {
            return null;
        }
        Dec temp;
        int topScope = 0;
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int scope = scopeStack.get(i);
            temp = (Dec) entryMap.get(scope);
            if (temp != null) {
                topScope = scope;
                break;
            }
        }
        temp = (Dec) entryMap.get(topScope);
        return temp;
    }

    public SymbolTable() {
        int current_scope = 0;
        int next_scope = 0;
        HashMap<String, HashMap<Integer, Dec>> entries = new HashMap<String, HashMap<Integer, Dec>>();
        Stack<Integer> scopeStack = new Stack<Integer>();

    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol Table:");
        sb.append('\n');
        Set Stringkey = entries.keySet();
        Iterator<String> stringIterator = Stringkey.iterator();
        while (stringIterator.hasNext()) {
            String ident = stringIterator.next();
            sb.append("Ident " + ident + ", scope number = ");
            Map<Integer, Dec> entryMap = entries.get(ident);
            Set decScope = entryMap.keySet();
            Iterator<Integer> integerIterator = decScope.iterator();
            while (integerIterator.hasNext()) {
                int scope = integerIterator.next();
                sb.append(scope + ", ");
            }
            sb.append('\n');
        }

        return sb.toString();
    }

}
