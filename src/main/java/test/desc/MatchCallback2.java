package test.desc;

import test.node.PlanNode;

public interface MatchCallback2<T1 extends PlanNode, T2 extends PlanNode> {
  PlanNode apply(T1 node1, T2 node2);
}
