# Java模式匹配
## 说明
- 功能：模仿Scala实现模式匹配，能够简单匹配多层代数数据类型(GADT)
- 依赖环境: `jdk8` (`lib/tools.jar`)
- 编译: `mvn clean package`
- 主类: `test.Main`，含有几个例子，可运行
- 原理: 通过`jsr269`功能定义注解处理器(`test.desc.DescribableNodeProcessor`)，在编译之前使用模板批量生成描述类(`XxxNodeDesc`)，能减少维护描述大量描述节点的成本


## Demo
例1: 通过上下文根节点获取匹配的子节点，能自动做类型推导:
```java
NodePatternRegistry patterns = new NodePatternRegistry();
// simplify
patterns.add(
  MultiplyNode(any(), NumNode(0)).then(ctx -> new NumNode(0)),
  MultiplyNode(NumNode(0), any()).then(ctx -> new NumNode(0)),
  MultiplyNode(any(), NumNode(1)).then(ctx -> ctx.root.left),
  MultiplyNode(NumNode(1), any()).then(ctx -> ctx.root.right)
);
```

例2: 通过别名访问匹配的子节点，不能做类型推导，需要强转:
```java
NodePatternRegistry patterns = new NodePatternRegistry();
patterns.add(JoinNode(JoinNode(named("a"), named("b")).type(INNER), named("c")).type(INNER).then(ctx -> {
  // no type inference
  return new JoinNode(INNER, ctx.get("a"), new JoinNode(INNER, ctx.get("b"), ctx.get("c")));
}));
```

例3：通过描述节点访问匹配的子节点，能自动做类型推导：
```java
NodePatternRegistry patterns = new NodePatternRegistry();
JoinNodeDesc<ScanNode, ScanNode> desc1 = JoinNode(ScanNode(), ScanNode());
ScanNodeDesc desc2 = _ScanNode("a");

patterns.add(JoinNode(desc1.type(INNER), desc2).type(INNER).then(ctx -> {
  // auto type inference
  JoinNode<ScanNode, ScanNode> left = ctx.get(desc1);
  ScanNode right = ctx.get(desc2);
  // cast to ScanNode
  ScanNode right2 = ctx.get("a");
  assert right == right2;
  return new JoinNode<>(INNER, left.left, new JoinNode(INNER, left.right, right));
}));
```