namespace scala testthrift

service Testthrift {
  i32 add(1:i32 num1, 2:i32 num2),
  void ping()
}

service Mc {
  string getKey(1:string key),
  void setKey(1:string key, string value)
}

service Mq {
  void publish(1:string name, 2:string msg),
  string receive(1:string name)
}