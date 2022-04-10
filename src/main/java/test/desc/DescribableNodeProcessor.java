package test.desc;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("test.desc.Describable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DescribableNodeProcessor extends AbstractProcessor {
  //语法树
  protected JavacTrees trees;

  //构建语法树节点
  protected TreeMaker treeMaker;

  //创建标识符的对象
  protected Names names;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    System.out.println("#############okkkkkkkkkkkkk");

    this.trees = JavacTrees.instance(processingEnv);
    Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
    this.treeMaker = TreeMaker.instance(context);
    this.names = Names.instance(context);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Describable.class);

      List<GenerateFileVars> files = new ArrayList<>();
      for (Element element : set) {
        String packageName = element.getEnclosingElement().toString();
        String className = element.getSimpleName().toString();
        files.add(generateDescJavaFile(element, packageName, className));
      }

      if (!files.isEmpty()) {
        generateHelperJavaFile(files);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return true;
  }

  private void generateHelperJavaFile(List<GenerateFileVars> files) throws IOException {
    VelocityContext context = new VelocityContext();
    context.put("nodes", files);
    Template template = Velocity.getTemplate("src/main/resources/NodePatternHelper.vm", "UTF-8");
    JavaFileObject descJavaFile = processingEnv.getFiler().createSourceFile("test.desc.NodePatternHelper");
    try (Writer writer = descJavaFile.openWriter()) {
      template.merge(context, writer);
    }
  }

  private GenerateFileVars generateDescJavaFile(Element element, String packageName, String nodeClassName) throws IOException {
    GenerateFileVars generateFileVars = getGenerateFileVars(element, packageName, nodeClassName);
    VelocityContext context = new VelocityContext();
    context.put("nodeClassName", generateFileVars.nodeClassName);
    context.put("classGenericType", generateFileVars.classGenericType);
    context.put("varsConstructorParams", generateFileVars.varsConstructorParams);
    context.put("childrenConstructorParams", generateFileVars.childrenConstructorParams);
    context.put("descGenericType", generateFileVars.descGenericType);
    context.put("builderGenericType", generateFileVars.builderGenericType);
    context.put("builderDescGenericType", generateFileVars.builderDescGenericType);
    context.put("varsBuilderParamTypes", generateFileVars.varsBuilderParamTypes);
    context.put("childrenBuilderParamTypes", generateFileVars.childrenBuilderParamTypes);
    context.put("varsBuilderParams", generateFileVars.varsBuilderParams);
    context.put("childrenBuilderParams", generateFileVars.childrenBuilderParams);
    context.put("vars", generateFileVars.vars);
    context.put("children", generateFileVars.children);
    Template template = Velocity.getTemplate("src/main/resources/NodeDesc.vm", "UTF-8");

    JavaFileObject descJavaFile = processingEnv.getFiler().createSourceFile("test.desc." + nodeClassName + "Desc");
    try (Writer writer = descJavaFile.openWriter()) {
      template.merge(context, writer);
    }
    return generateFileVars;
  }

  private GenerateFileVars getGenerateFileVars(Element element, String packageName, String nodeClassName) {
    JCTree jcTree = trees.getTree(element);
    List<Var> vars = new ArrayList<>();
    List<Var> children = new ArrayList<>();
    List<JCTree.JCTypeParameter> typeParameters = new ArrayList<>();
    Map<String, JCTree.JCTypeParameter> typeParameterMap = new LinkedHashMap<>();
    jcTree.accept(new TreeTranslator() {
      @Override
      public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        typeParameters.addAll(jcClassDecl.getTypeParameters());
        typeParameterMap.putAll(jcClassDecl.getTypeParameters()
            .stream()
            .collect(Collectors.toMap(
                it -> it.getName().toString(),
                it -> it)
            ));
        for (JCTree tree : jcClassDecl.defs) {
          // skip methods
          if (!tree.getKind().equals(Tree.Kind.VARIABLE)) {
            continue;
          }
          // skip inner class fields
          JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) tree;
          if (!variableDecl.sym.owner.getQualifiedName().toString().equals(((Symbol.ClassSymbol) element).getQualifiedName().toString())) {
            continue;
          }

          String varName = variableDecl.name.toString();
          String varType = getLiteralVarType(variableDecl, typeParameterMap);
          String varTypeWithoutParam = getOriginVarType(variableDecl, typeParameterMap);
          boolean isChild = varTypeWithoutParam.startsWith("test.node.") && varTypeWithoutParam.endsWith("Node");
          if (isChild) {
            varType = "NodePattern<" + varType + ">";
          }

          String descGenericType = "";
          if (!typeParameterMap.isEmpty()) {
            descGenericType = "<" + typeParameterMap.keySet().stream().collect(Collectors.joining(", ")) + ">";
          }
          Var var = new Var(varType, varName, descGenericType, isChild);
          vars.add(var);
          if (isChild) {
            children.add(var);
          }
        }
        super.visitClassDef(jcClassDecl);
      }
    });

    String classDeclaredGenericType = "";
    String varsConstructorParams = "";
    String childrenConstructorParams = "";
    String builderGenericType = "";
    String builderDescGenericType = "";
    String varsBuilderParamTypes = "";
    String childrenBuilderParamTypes = "";
    String varsBuilderParams = "";
    String childrenBuilderParams = "";
    if (!typeParameters.isEmpty()) {
      classDeclaredGenericType = "<" + typeParameters.stream().map(it -> it.toString()).collect(Collectors.joining(", ")) + ">";
    }
    if (!vars.isEmpty()) {
      varsConstructorParams = vars.stream()
          .map(var -> var.type + " " + var.name)
          .collect(Collectors.joining(", "));
      varsBuilderParamTypes = varsConstructorParams;
      varsBuilderParams = vars.stream()
          .map(var -> var.name)
          .collect(Collectors.joining(", "));
    }
    if (!children.isEmpty()) {
      childrenConstructorParams = children.stream()
          .map(var -> var.type + " " + var.name)
          .collect(Collectors.joining(", "));

      childrenBuilderParamTypes = children.stream()
          .map(var -> var.type + " " + var.name)
          .collect(Collectors.joining(", "));

      childrenBuilderParams = children.stream()
          .map(var -> var.name)
          .collect(Collectors.joining(", "));
    }

    String descGenericType = "";
    if (!typeParameterMap.isEmpty()) {
      descGenericType = "<" + typeParameterMap.keySet().stream().collect(Collectors.joining(", ")) + ">";
    }

    return new GenerateFileVars(nodeClassName, classDeclaredGenericType, varsConstructorParams,
        childrenConstructorParams, descGenericType, builderGenericType, builderDescGenericType,
        varsBuilderParamTypes, childrenBuilderParamTypes, varsBuilderParams, childrenBuilderParams, vars, children);
  }

  private String getLiteralVarType(JCTree.JCVariableDecl variableDecl, Map<String, JCTree.JCTypeParameter> typeParameters) {
    String varType = variableDecl.vartype.type.toString();

    if (varType.startsWith("java.lang.")) {
      varType = varType.substring("java.lang.".length());
    }
    return varType;
  }

  private String getFullDescType(JCTree.JCVariableDecl variableDecl, Map<String, JCTree.JCTypeParameter> typeParameters) {
    String varType = variableDecl.vartype.type.tsym.toString();

    JCTree.JCTypeParameter jcTypeParameter = typeParameters.get(varType);
    if (jcTypeParameter != null) {
      List<JCTree.JCExpression> genericBound = jcTypeParameter.getBounds();
      if (genericBound != null && !genericBound.isEmpty()) {
        varType = jcTypeParameter.toString();
      }
    }

    if (varType.startsWith("java.lang.")) {
      varType = varType.substring("java.lang.".length());
    }
    return varType;
  }

  private String getOriginVarType(JCTree.JCVariableDecl variableDecl, Map<String, JCTree.JCTypeParameter> typeParameters) {
    String varType = variableDecl.vartype.type.tsym.toString();

    JCTree.JCTypeParameter jcTypeParameter = typeParameters.get(varType);
    if (jcTypeParameter != null) {
      List<JCTree.JCExpression> genericBound = jcTypeParameter.getBounds();
      if (genericBound != null && !genericBound.isEmpty()) {
        varType = genericBound.get(0).type.toString();
      }
    }

    if (varType.startsWith("java.lang.")) {
      varType = varType.substring("java.lang.".length());
    }
    return varType;
  }

  public static class GenerateFileVars {
    public final String nodeClassName;
    public final String classGenericType;
    public final String varsConstructorParams;
    public final String childrenConstructorParams;
    public final String descGenericType;
    public final String builderGenericType;
    public final String builderDescGenericType;
    public final String varsBuilderParamTypes;
    public final String childrenBuilderParamTypes;
    public final String varsBuilderParams;
    public final String childrenBuilderParams;
    public final List<Var> vars;
    public final List<Var> children;

    public GenerateFileVars(String nodeClassName, String classGenericType, String varsConstructorParams,
                            String childrenConstructorParams, String descGenericType, String builderGenericType,
                            String builderDescGenericType, String varsBuilderParamTypes,
                            String childrenBuilderParamTypes, String varsBuilderParams, String childrenBuilderParams,
                            List<Var> vars, List<Var> children) {
      this.nodeClassName = nodeClassName;
      this.classGenericType = classGenericType;
      this.varsConstructorParams = varsConstructorParams;
      this.childrenConstructorParams = childrenConstructorParams;
      this.descGenericType = descGenericType;
      this.builderGenericType = builderGenericType;
      this.builderDescGenericType = builderDescGenericType;
      this.varsBuilderParamTypes = varsBuilderParamTypes;
      this.childrenBuilderParamTypes = childrenBuilderParamTypes;
      this.varsBuilderParams = varsBuilderParams;
      this.childrenBuilderParams = childrenBuilderParams;
      this.vars = vars;
      this.children = children;
    }

    public String getNodeClassName() {
      return nodeClassName;
    }

    public String getClassGenericType() {
      return classGenericType;
    }

    public String getVarsConstructorParams() {
      return varsConstructorParams;
    }

    public String getChildrenConstructorParams() {
      return childrenConstructorParams;
    }

    public String getDescGenericType() {
      return descGenericType;
    }

    public String getBuilderGenericType() {
      return builderGenericType;
    }

    public String getBuilderDescGenericType() {
      return builderDescGenericType;
    }

    public String getVarsBuilderParamTypes() {
      return varsBuilderParamTypes;
    }

    public String getChildrenBuilderParamTypes() {
      return childrenBuilderParamTypes;
    }

    public String getVarsBuilderParams() {
      return varsBuilderParams;
    }

    public String getChildrenBuilderParams() {
      return childrenBuilderParams;
    }

    public List<Var> getVars() {
      return vars;
    }

    public List<Var> getChildren() {
      return children;
    }
  }

  public static class Var {
    public final String type;
    public final String name;
    public final String descGenericType;
    public final boolean isChild;

    public Var(String type, String name, String descGenericType, boolean isChild) {
      this.type = type;
      this.name = name;
      this.descGenericType = descGenericType;
      this.isChild = isChild;
    }

    public String getType() {
      return type;
    }

    public String getName() {
      return name;
    }

    public String getDescGenericType() {
      return descGenericType;
    }

    public boolean getIsChild() {
      return isChild;
    }
  }
}
