package code.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import org.objectweb.asm.*;

public class MethodExtractor {
	
	public List<String> methodList = new ArrayList<String>();
	public Map<String,Visitor> visitorHash = new HashMap<String,Visitor>();
	
	class UserClassLoader extends ClassLoader
	{
		Class defineClass(byte[] classData)
		{
			//ClassLoader loader = (ClassLoader) Thread.currentThread().getContextClassLoader();
			Class userDefinedClass = defineClass(null, classData, 0, classData.length);
			return userDefinedClass;
		}
	}
	
	public class ClassPrinter implements ClassVisitor {
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			//System.out.println(name + " extends " + superName + " {");
		}
		public void visitSource(String source, String debug) {
		}
		public void visitOuterClass(String owner, String name, String desc) {
		}
		public AnnotationVisitor visitAnnotation(String desc,
				boolean visible) {
			return null;
		}
		public void visitAttribute(Attribute attr) {
		}
		public void visitInnerClass(String name, String outerName,
				String innerName, int access) {
		}
		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			//System.out.println(" " + desc + " " + name);
			return null;
		}
		public MethodVisitor visitMethod(int access, String name,
				String desc, String signature, String[] exceptions) {
			//System.out.println(" " + name + desc);
			Visitor v = new Visitor();
			visitorHash.put(name,v);
			v.methodName=name;
			return v;
		}
		public void visitEnd() {
			//System.out.println("}");
		}
	}
	
	public class Visitor implements MethodVisitor 
	{
		public boolean visited;
		public int lineNumber = -1;
		public String methodName;
		public String argumentTypes;
		public String modifierAndReturnType;

    public String getMethodSignature() {
      return modifierAndReturnType + " " + methodName + "(" + argumentTypes + ")";
    }
		
		@Override
		public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void visitAttribute(Attribute arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitCode() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitEnd() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitFieldInsn(int arg0, String arg1, String arg2,
				String arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
				Object[] arg4) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitIincInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitInsn(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitIntInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitJumpInsn(int arg0, Label arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitLabel(Label arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void visitLdcInsn(Object arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitLineNumber(int arg0, Label arg1) {
			//System.out.println(arg0 + "//" + arg1.toString());
			if (!visited)
			{
				lineNumber = arg0;
			}
			visited=true;
		}

		@Override
		public void visitLocalVariable(String arg0, String arg1, String arg2,
				Label arg3, Label arg4, int arg5) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitMaxs(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitMethodInsn(int arg0, String arg1, String arg2,
				String arg3) {
			//System.out.println("--" + arg0 + "//" + arg2 + "//" + arg3);
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitMultiANewArrayInsn(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int arg0,
				String arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
				Label[] arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
				String arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitTypeInsn(int arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitVarInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	}

	public static void main(String[] args) throws Exception
	{ 
		File f = new File("C:\\Users\\Allen\\Downloads\\Documents\\3rd Year\\yahoohackday\\Foo.class");
		extractMethods(f);
	}
	
	public static MethodExtractor extractMethods(File f) throws Exception
	{
		MethodExtractor me = new MethodExtractor();
		ClassReader cr = new ClassReader(new FileInputStream(f));
		ClassPrinter cp = me.new ClassPrinter();
		cr.accept(cp,0);
	
		me.populateMethodList(f);
		
		for (String methodName : me.methodList)
		{
			Visitor v = me.visitorHash.get(methodName);
			//System.out.println(v.modifierAndReturnType + " " + v.methodName + "(" + v.argumentTypes + ")" + ": " + v.lineNumber);
		}
		
		return me;
	}
	
	private void populateMethodList(File f)
	{		
		FileInputStream fis;
		BufferedInputStream bis;
		DataInputStream dis;
		
		try
		{
			byte[] inputCode = new byte[(int) f.length()];
			fis = new FileInputStream(f);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			try
			{
				dis.readFully(inputCode);
			}
			catch (IOException e)
			{
			}
			
			Class userDefinedClass = (new UserClassLoader()).defineClass(inputCode);

      {
        Method[] methods = userDefinedClass.getDeclaredMethods();
        for (Method method : methods)
        {
          methodList.add(method.getName());
          Visitor v = visitorHash.get(method.getName());
          String parameterTypes = "";
          for (Class c : method.getParameterTypes())
          {
            if (parameterTypes != "")
            {
              parameterTypes += ",";
            }
            parameterTypes += c.getSimpleName();
          }
          String returnType;
          if (method.getGenericReturnType() instanceof Class) {
            returnType = ((Class)method.getGenericReturnType()).getSimpleName();
          } else {
            returnType = method.getGenericReturnType().toString();
          }
          v.modifierAndReturnType = Modifier.toString(method.getModifiers()) + " " + returnType;
          v.argumentTypes = parameterTypes;
        }
      }

      {
        Constructor[] methods = userDefinedClass.getDeclaredConstructors();
        for (Constructor method : methods)
        {
          Visitor v = visitorHash.get("<init>");
          if (v != null) {
            methodList.add(userDefinedClass.getSimpleName());
            String parameterTypes = "";
            for (Class c : method.getParameterTypes())
            {
              if (parameterTypes != "")
              {
                parameterTypes += ",";
              }
              parameterTypes += c.getSimpleName();
            }
            v.methodName = userDefinedClass.getSimpleName();
            v.modifierAndReturnType = Modifier.toString(method.getModifiers());
            v.argumentTypes = parameterTypes;
          }         
        }
      }
		}
		catch (FileNotFoundException e)
		{
      System.err.println("Oops, file not found: " + f);
		}
		
		return;
	}
}
