package test.desc;

import test.node.PlanNode;

import java.util.ArrayList;
import java.util.List;

public class NodePatternRegistry {
  private List<NodePatternAndCallback<PlanNode, PlanNode>> patterns = new ArrayList<>();

  public NodePatternRegistry add(NodePatternAndCallback<? extends PlanNode, ? extends PlanNode>... patterns) {
    for (NodePatternAndCallback<? extends PlanNode, ? extends PlanNode> pattern : patterns) {
      this.patterns.add((NodePatternAndCallback) pattern);
    }
    return this;
  }

  public <T extends PlanNode, R extends PlanNode> R match(T node) {
    for (NodePatternAndCallback<? extends PlanNode, ? extends PlanNode> pattern : patterns) {
      if (pattern.pattern.match(node)) {
        MatchContext<T> matchContext = new MatchContext(node);
        pattern.pattern.buildContext(matchContext, node);
        return (R) pattern.callback.apply(matchContext);
      }
    }
    return (R) node;
  }
}
