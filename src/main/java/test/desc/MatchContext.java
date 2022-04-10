package test.desc;

import test.node.PlanNode;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MatchContext<T extends PlanNode> {
  public final T root;
  private Map<String, PlanNode> namedNodes = new LinkedHashMap<>();
  private Map<NodePattern, PlanNode> patternNodes = new IdentityHashMap();

  public MatchContext(T root) {
    this.root = root;
  }

  public <T extends PlanNode> T get(String name) {
    return (T) namedNodes.get(name);
  }

  public <T extends PlanNode> T get(NodePattern<T> pattern) {
    return (T) patternNodes.get(pattern);
  }

  public MatchContext set(String name, PlanNode node) {
    namedNodes.put(name, node);
    return this;
  }

  public MatchContext set(NodePattern pattern, PlanNode node) {
    patternNodes.put(pattern, node);
    return this;
  }

  public Map<String, PlanNode> nodes() {
    return namedNodes;
  }
}
