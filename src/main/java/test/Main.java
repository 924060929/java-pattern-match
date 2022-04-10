package test;

import test.desc.*;
import test.node.*;

import static test.desc.NodePatternHelper.*;
import static test.node.JoinNode.Type.*;

public class Main {
  public static void main(String[] args) {
    // test
    JoinNode<JoinNode<ScanNode, ScanNode>, ScanNode> joinNode = new JoinNode<>(
      INNER,
      new JoinNode<>(INNER, new ScanNode("table1"), new ScanNode("table2")),
      new ScanNode("table3")
    );

    test0();
    test1(joinNode);
    test2(joinNode);
    test3(joinNode);
  }

  public static void test0() {
    System.out.println("test0");
    NodePatternRegistry patterns = new NodePatternRegistry();
    // simplify
    patterns.add(
      MultiplyNode(any(), NumNode(0)).then(ctx -> new NumNode(0)),
      MultiplyNode(NumNode(0), any()).then(ctx -> new NumNode(0)),
      MultiplyNode(any(), NumNode(1)).then(ctx -> ctx.root.left),
      MultiplyNode(NumNode(1), any()).then(ctx -> ctx.root.right)
    );

    MultiplyNode<Integer> multiplyNode = new MultiplyNode<>(
      new NumNode<>(12),
      new NumNode<>(1)
    );

    System.out.println("origin tree: " + multiplyNode);
    PlanNode result = patterns.match(multiplyNode);
    System.out.println("result tree: " + result);
    System.out.println();
  }

  // way 1: get by root
  public static void test1(PlanNode node) {
    System.out.println("test1");
    NodePatternRegistry patterns = new NodePatternRegistry();
    patterns.add(JoinNode(INNER, JoinNode(INNER, any(), any()), any()).then(ctx -> {
      // auto type inference
      JoinNode left = ctx.root.left;
      PlanNode right = ctx.root.right;
      return new JoinNode(INNER, left.left, new JoinNode(INNER, left.right, right));
    }));

    System.out.println("origin tree: " + node);
    PlanNode result = patterns.match(node);
    System.out.println("result tree: " + result);
    System.out.println();
  }

  // way 2: get by name
  public static void test2(PlanNode node) {
    System.out.println("test2");
    NodePatternRegistry patterns = new NodePatternRegistry();
    patterns.add(JoinNode(INNER, JoinNode(_ScanNode("a"), named("b")), named("c")).then(ctx -> {
      // get ScanNode by "a", return PlanNode, even "a" is a ScanNode
      PlanNode a = ctx.get("a");
      return new JoinNode(INNER, a, new JoinNode(INNER, ctx.get("b"), ctx.get("c")));
    }));

    System.out.println("origin tree: " + node);
    PlanNode result = patterns.match(node);
    System.out.println("result tree: " + result);
    System.out.println();
  }

  // way 3: get by desc
  public static void test3(PlanNode node) {
    System.out.println("test3");
    NodePatternRegistry patterns = new NodePatternRegistry();
    JoinNodeDesc<ScanNode, ScanNode> desc1 = JoinNode(INNER, ScanNode(), ScanNode());
    ScanNodeDesc desc2 = _ScanNode("a");

    patterns.add(JoinNode(INNER, desc1, desc2).then(ctx -> {
      // auto type inference
      JoinNode<ScanNode, ScanNode> left = ctx.get(desc1);
      ScanNode right = ctx.get(desc2);
      // May be unsafe operation: cast PlanNode to ScanNode
      ScanNode right2 = (ScanNode) ctx.get("a");
      assert right == right2;
      return new JoinNode<>(INNER, left.left, new JoinNode(INNER, left.right, right));
    }));

    System.out.println("origin tree: " + node);
    PlanNode result = patterns.match(node);
    System.out.println("result tree: " + result);
    System.out.println();
  }

//  // way4
//  @PatternMatcher
//  public static void test4(PlanNode node) {
//    // use annotation process to change bytecode
//
////    switch (node) {
////      case JoinNode(JoinNode(a = ScanNode(), b = ScanNode()), c = ScanNode()): new JoinNode(INNER, a, new JoinNode(INNER, b, c));
////    }
//  }
}
