package test.node;

import java.util.List;

public interface PlanNode<T extends PlanNode> {
  List<PlanNode> getChildren();
}
