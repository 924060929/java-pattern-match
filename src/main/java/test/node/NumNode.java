package test.node;

import test.desc.Describable;

import java.util.ArrayList;
import java.util.List;

@Describable
public class NumNode<T extends Number> implements PlanNode<NumNode<T>> {
  public final T value;

  public NumNode(T value) {
    this.value = value;
  }

  @Override
  public List<PlanNode> getChildren() {
    return new ArrayList<>();
  }

  @Override
  public String toString() {
    return "NumNode(value=" + value + ")";
  }
}
