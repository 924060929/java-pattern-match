package test.desc;

import test.node.PlanNode;

public interface MatchCallback3<T1 extends PlanNode, T2 extends PlanNode, T3 extends PlanNode> {
  PlanNode apply(T1 node1, T2 node2, T3 node3);
}
