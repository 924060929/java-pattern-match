package test.desc;

import test.node.PlanNode;

import java.util.Optional;

public class AnyNodePattern<T extends PlanNode> implements NodePattern<T> {
  private Optional<String> _named = Optional.empty();

  @Override
  public boolean match(PlanNode planNode) {
    return planNode != null;
  }

  @Override
  public void buildContext(MatchContext context, PlanNode planNode) {
    context.set(this, planNode);
    if (_named.isPresent()) {
      context.set(_named.get(), planNode);
    }
  }

  @Override
  public AnyNodePattern<T> named(String name) {
    this._named = Optional.ofNullable(name);
    return this;
  }
}
