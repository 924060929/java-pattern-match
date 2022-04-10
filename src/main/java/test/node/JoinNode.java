package test.node;

import test.desc.Describable;

import java.util.ArrayList;
import java.util.List;

@Describable
public class JoinNode<T1 extends PlanNode, T2 extends PlanNode> implements PlanNode<JoinNode<T1, T2>> {
  public final Type type;
  public final T1 left;
  public final T2 right;

  public enum Type {
    INNER, LEFT_OUTER, RIGHT_OUTER, FULL_OUTER, CROSS, ANTI, SEMI
  }

  public JoinNode(Type type, T1 left, T2 right) {
    this.type = type;
    this.left = left;
    this.right = right;
  }

  public Type getType() {
    return type;
  }

  public T1 getLeft() {
    return left;
  }

  public T2 getRight() {
    return right;
  }

  @Override
  public List<PlanNode> getChildren() {
    List<PlanNode> children = new ArrayList<>();
    children.add(left);
    children.add(right);
    return children;
  }

  @Override
  public String toString() {
    return "Join(type=" + type + ", left=" + left + ", right=" + right + ")";
  }
}
