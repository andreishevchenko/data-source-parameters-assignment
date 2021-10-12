# Data sources and parameters

### Task description

```scala
case class DataSource(id: String, name: String, parameters: Set[Parameter])
case class Parameter(id: String, name: String, category: Category)
case class Category(id: String, name: String, parent: Option[Category])
```

A data source is a simple class with an ID, a name and a set of parameters.

Parameters belong to a category.
Each category can be a sub-category for another category, forming a tree-like structure.

We want to create a database structure to support data sources that know about a set of parameters.

The `src/main/resources/db/migrations/V1__data_sources.sql` file holds the currently incomplete definition of a data source.

The `tech.responsibly.DataSourceDao` class contains the DAO implementation that encapsulates the data sources.
It only has one method, `getById` which attempts to find a data source by its ID. It does not currently know about parameters.

### Requirements

- Complete the database definition with the concepts of `Categories` and `Parameters`
- Modify the select statement in the `getById` method of the `DataSourceDao` to return the full `DataSource`, with all the `Parameters` and `Categories`
- Write unit tests for relevant test cases

### Example output

```scala
DataSource(
  id = "datasource-1",
  name = "Data source 1",
  parameters = Set(
    Parameter(
      id = "parameter-1",
      name = "Non-discrimination",
      category = Category(
        id = "category-1",
        name = "Diversity and inclusion",
        parent = Some(
          Category(
            id = "category-2",
            name = "Social impact",
            parent = None
          )
        )
      )
    ),
    Parameter(
      id = "parameter-2",
      name = "Bio diversity",
      category = Category(
        id = "category-3",
        name = "Environmental impact",
        parent = None
      )
    )
  )
)
```

### Delivery

- Clone the repository
- Push your changes to your own GitHub repository
- If the repository is private, please provide access to `synnks` and `jvous`