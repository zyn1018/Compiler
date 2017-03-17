package cop5556sp17;


import cop5556sp17.AST.Dec;

import java.util.*;


public class SymbolTable {

    int current_scope, next_scope;

    class Entry {
        public Entry(int scope, Dec dec) {
            this.scope = scope;
            this.dec = dec;
        }

        int scope;
        Dec dec;

    }

    Map<String, HashMap<Integer, Entry>> entries = new HashMap<String, HashMap<Integer, Entry>>();
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
          if(entryMap != null ){
              Entry temp = (Entry) entryMap.get(current_scope);
              if(temp != null){
                  return false;
              }
              entryMap.put(current_scope, new Entry(current_scope, dec));
          }else{
              entryMap.put(current_scope, new Entry(current_scope, dec));
          }
        entries.put(ident, (HashMap<Integer, Entry>) entryMap);
        return true;

    }

    public Dec lookup(String ident) {

        HashMap entryMap = entries.get(ident);
        if (entryMap == null) {
            return null;
        }
        Entry temp;
        int topScope = current_scope;
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int scope = scopeStack.get(i);
            temp = (Entry) entryMap.get(scope);
            if(temp != null){
                topScope = scope;
                break;
            }
        }
        temp = (Entry)entryMap.get(topScope);
        return temp.dec;
    }

    public SymbolTable() {
        int next_scope = 0;
        HashMap<String, HashMap<Integer, Entry>> entries = new HashMap<String, HashMap<Integer, Entry>>();
        Stack<Integer> scopeStack = new Stack<Integer>();

    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol Table:");
        sb.append("\n");
        Set entryMapSet = entries.entrySet();

        for(int i = scopeStack.size() - 1; i >=0; i--) {
            sb.append("In scope number = " + scopeStack.get(i) + " : ");
            Iterator<HashMap.Entry<String, HashMap<Integer, Entry>>> it = entries
                    .entrySet().iterator();

            while (it.hasNext()) {

                // entry.getKey() 返回与此项对应的键
                // entry.getValue() 返回与此项对应的值

                Map.Entry entry = (Map.Entry)it.next();
                System.out.print("/n"+entry.getKey());

                HashMap tmp_in_hashmap=(HashMap)entry.getValue();

                Iterator<Map.Entry<Integer, Entry>> entryIterator = tmp_in_hashmap
                        .entrySet().iterator();

                while(entryIterator.hasNext()){
                    Map.Entry in_entry = (Map.Entry)entryIterator.next();
                    System.out.println("->"+in_entry.getKey()+":");
                    int[] array=(int[])in_entry.getValue();
                    for(int each:array){
                        System.out.print(each+" ");
                    }
                }

            }

        }
        return "";
    }


}
