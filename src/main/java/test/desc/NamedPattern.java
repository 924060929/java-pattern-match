package test.desc;

import test.node.PlanNode;

public class NamedPattern implements NodePattern<PlanNode> {
  public String name;

  public NamedPattern(String name) {
    this.name = name;
  }

  @Override
  public boolean match(PlanNode planNode) {
    return true;
  }

  @Override
  public void buildContext(MatchContext context, PlanNode planNode) {
    context.set(name, planNode);
  }

  @Override
  public NamedPattern named(String name) {
    this.name = name;
    return this;
  }
}
