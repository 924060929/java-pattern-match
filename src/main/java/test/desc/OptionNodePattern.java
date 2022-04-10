package test.desc;

import test.node.PlanNode;

import java.util.Optional;

public class OptionNodePattern<T extends PlanNode> implements NodePattern<T> {
  private Optional<String> _named = Optional.empty();
  private Optional<NodePattern<T>> nodePattern = Optional.empty();

  @Override
  public boolean match(PlanNode planNode) {
    if (planNode != null && nodePattern.isPresent()) {
      return nodePattern.get().match(planNode);
    }
    return true;
  }

  @Override
  public void buildContext(MatchContext context, PlanNode planNode) {
    if (planNode != null) {
      context.set(this, planNode);
      if (_named.isPresent()) {
        context.set(_named.get(), planNode);
      }
      if (nodePattern.isPresent()) {
        nodePattern.get().buildContext(context, planNode);
      }
    }
  }

  @Override
  public OptionNodePattern<T> named(String name) {
    this._named = Optional.ofNullable(name);
    return this;
  }

  public OptionNodePattern<T> pattern(NodePattern<T> nodePattern) {
    this.nodePattern = Optional.ofNullable(nodePattern);
    return this;
  }
}
