type Person object {
    public {
        string name;
    }

    public function foo(string... names) returns string;

    public function bar(string... names) returns string;
};

public function Person::foo(string... names) returns string {
    return names[0] + " foo";
}

public function Person::bar(string... names) returns string {
    return names[0] + " bar";
}

public function testAttachFunctionsWithIdenticalRestParams()  returns string {
    Person p = new;
    return p.foo("hello", "world");
}