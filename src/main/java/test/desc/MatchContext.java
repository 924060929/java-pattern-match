package test.desc;

import test.node.PlanNode;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MatchContext<T extends PlanNode> {
  public final T root;
  private Map<String, PlanNode> namedNodes = new LinkedHashMap<>();
  private Map<NodePattern, PlanNode> patternNodes = new IdentityHashMap();

  public MatchContext(T root) {
    this.root = root;
  }

  public PlanNode get(String name) {
    return namedNodes.get(name);
  }

  public Void get(NoneNodePattern pattern) {
    return null;
  }

  public PlanNode get(AnyNodePattern pattern) {
    return patternNodes.get(pattern);
  }

  public <T extends PlanNode> Optional<T> get(OptionNodePattern<T> pattern) {
    return Optional.ofNullable((T) patternNodes.get(pattern));
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
