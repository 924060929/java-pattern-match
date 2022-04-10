package test.desc;

import test.node.PlanNode;

import java.util.Optional;

public class NoneNodePattern<T extends PlanNode> implements NodePattern<T> {

  @Override
  public boolean match(PlanNode planNode) {
    return planNode == null;
  }

  @Override
  public void buildContext(MatchContext context, PlanNode planNode) {

  }

  @Override
  public NoneNodePattern<T> named(String name) {
    return this;
  }
}
