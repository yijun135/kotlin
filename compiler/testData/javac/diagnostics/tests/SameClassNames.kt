// FILE: pack/JavaClass.java
package pack;

public class JavaClass {

    public class List {
        public void test() {}
    }

    public JavaClassImpl getJavaClassImpl() { return new JavaClassImpl(); }

}

class JavaClassImpl extends JavaClass {

    List getList() { return null; }

    class List {
        List getList() { return null; }
        void doNothing() {}
    }
    
}

class List {}

// FILE: pack/Test.kt
package pack;

private fun test() = JavaClass().getJavaClassImpl().getList().getList().doNothing()
