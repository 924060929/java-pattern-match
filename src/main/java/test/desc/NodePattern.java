package test.desc;

import test.node.PlanNode;

public interface NodePattern<T extends PlanNode> {
  boolean match(PlanNode planNode);

  void buildContext(MatchContext context, PlanNode planNode);

  NodePattern<T> named(String name);
}
