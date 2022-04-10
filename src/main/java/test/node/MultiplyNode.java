package test.node;

import test.desc.Describable;

import java.util.ArrayList;
import java.util.List;

@Describable
public class MultiplyNode<T extends Number> implements PlanNode<MultiplyNode<T>> {
  public final NumNode<T> left;
  public final NumNode<T> right;

  public MultiplyNode(NumNode<T> left, NumNode<T> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public List<PlanNode> getChildren() {
    ArrayList<PlanNode> children = new ArrayList<>();
    children.add(left);
    children.add(right);
    return children;
  }

  @Override
  public String toString() {
    return "MultiplyNode(left=" + left + ", right=" + right + ")";
  }
}
