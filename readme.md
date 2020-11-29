# Widgets API

Use the widgets api to create, get, delete and update widgets on your board. You can
decide to change whether you want to save the widgets in an in-memory data structure (default behaviour)
or in an in-memory SQL database by setting the property `use-sql-repository` to `true` in `application.properties` file.

Use the following endpoints to manage your widgets:

- Create a widget 

```
[POST] /widgets

Payload:

{
    "width": 20,
    "height": 40,
    "x": 10,
    "y": 4,
    "z": 7
}
```
- Update a widget 

```
[PUT] /widgets/{id}

Payload:

{
    "width": 11,
    "height": 14,
    "x": 12,
    "y": 4,
    "z": 7
}
```

- Delete a widget 

```
[DELETE] /widgets/{id}
```

- Get a widget 

```
[GET] /widgets/{id}
```

- Get all widgets

```
[GET] /widgets?page=0&size=10
```
