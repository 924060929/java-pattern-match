# Java模式匹配
## 说明
- 功能：模仿Scala实现模式匹配，能够简单匹配多层代数数据类型(GADT)
- 依赖环境: `jdk8` (`lib/tools.jar`)
- 编译: `mvn clean package`
- 主类: `test.Main`，含有几个例子，可使用IDE运行
- 命令行运行: `java -jar target/pattern-match-*.jar`
- 原理: 通过`jsr269`功能定义注解处理器(`test.desc.DescribableNodeProcessor`)，在编译之前使用模板批量生成描述类(`XxxNodeDesc`)，能减少维护大量描述类的成本


## Demo
### 1. 完整例子
常量表达式化简：
```java
NodePatternRegistry patterns = new NodePatternRegistry();
// declare patterns and match callback
patterns.add(
  MultiplyNode(any(), NumNode(0)).then(ctx -> new NumNode(0)),
  MultiplyNode(NumNode(0), any()).then(ctx -> new NumNode(0)),
  MultiplyNode(any(), NumNode(1)).then(ctx -> ctx.root.left),
  MultiplyNode(NumNode(1), any()).then(ctx -> ctx.root.right)
);

// origin node tree
MultiplyNode<Integer> multiplyNode = new MultiplyNode<>(
  new NumNode<>(12),
  new NumNode<>(1)
);

System.out.println("origin tree: " + multiplyNode);
// match
PlanNode result = patterns.match(multiplyNode);
System.out.println("result tree: " + result);
```

输出为
```text
origin tree: MultiplyNode(left=NumNode(value=12), right=NumNode(value=1))
result tree: NumNode(value=12)
```


### 2. 通过上下文根节点获取匹配的子节点，能自动做类型推导
inner join 左结合转右结合：
```java
NodePatternRegistry patterns = new NodePatternRegistry();
patterns.add(JoinNode(INNER, JoinNode(INNER, any(), any()), any()).then(ctx -> {
  // auto type inference
  JoinNode left = ctx.root.left;
  PlanNode right = ctx.root.right;
  return new JoinNode(INNER, left.left, new JoinNode(INNER, left.right, right));
}));
```

### 3. 通过别名访问匹配的子节点，不能做类型推导，需要强转
inner join 左结合转右结合：
```java
NodePatternRegistry patterns = new NodePatternRegistry();
patterns.add(JoinNode(INNER, JoinNode(named("a"), named("b")), named("c")).then(ctx -> {
  // no type inference
  return new JoinNode(INNER, ctx.get("a"), new JoinNode(INNER, ctx.get("b"), ctx.get("c")));
}));
```

### 4. 通过描述节点访问匹配的子节点，能自动做类型推导
inner join 左结合转右结合：
```java
NodePatternRegistry patterns = new NodePatternRegistry();
JoinNodeDesc<ScanNode, ScanNode> desc1 = JoinNode(INNER, ScanNode(), ScanNode());
ScanNodeDesc desc2 = _ScanNode("a");
patterns.add(JoinNode(INNER, desc1, desc2).then(ctx -> {
  // auto type inference
  JoinNode<ScanNode, ScanNode> left = ctx.get(desc1);
  ScanNode right = ctx.get(desc2);
  // cast to ScanNode
  ScanNode right2 = ctx.get("a");
  assert right == right2;
  return new JoinNode<>(INNER, left.left, new JoinNode(INNER, left.right, right));
}));
```