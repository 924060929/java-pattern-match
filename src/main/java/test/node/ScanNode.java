package test.node;

import test.desc.Describable;

import java.util.ArrayList;
import java.util.List;

@Describable
public class ScanNode implements PlanNode<ScanNode> {
  public final String tableName;

  public ScanNode(String name) {
    this.tableName = name;
  }

  public String getTableName() {
    return tableName;
  }

  @Override
  public List<PlanNode> getChildren() {
    return new ArrayList<>();
  }

  @Override
  public String toString() {
    return "ScanNode(tableName=" + tableName + ")";
  }
}
