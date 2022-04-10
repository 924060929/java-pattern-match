package test.desc;

import test.node.PlanNode;

public class NodePatternAndCallback<T extends PlanNode, R> {
  public final NodePattern<T> pattern;
  public final MatchCallback1<MatchContext, R> callback;

  public NodePatternAndCallback(NodePattern<T> pattern, MatchCallback1<MatchContext, R> callback) {
    this.pattern = pattern;
    this.callback = callback;
  }
}
