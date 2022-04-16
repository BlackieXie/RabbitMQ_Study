# 基本概念

## 什么是消息队列

**消息**指的是两个应用间传递的数据。数据的类型有很多种形式，可能只包含文本字符串，也可能包含嵌入对象。

**“消息队列(Message Queue)”是在消息的传输过程中保存消息的容器**。在消息队列中，通常有生产者和消费者两个角色。生产者只负责发送数据到消息队列，谁从消息队列中取出数据处理，他不管。消费者只负责从消息队列中取出数据处理，他不管这是谁发送的数据。

![image-20220413222152357](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413222152357.png)

## 消息队列优缺点

主要有三个作用：

​		1.**解耦**。如图所示。假设有系统B、C、D都需要系统A的数据，于是系统A调用三个方法发送数据到B、C、D。这时，系统D不需要了，那就需要在系统A把相关的代码删掉。假设这时有个新的系统E需要数据，这时系统A又要增加调用系统E的代码。为了降低这种强耦合，就可以使用MQ，**系统A只需要把数据发送到MQ，其他系统如果需要数据，则从MQ中获取即可**。

![image-20220413222216248](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413222216248.png)

​		**2.异步**。如图所示。一个客户端请求发送进来，系统A会调用系统B、C、D三个系统，同步请求的话，响应时间就是系统A、B、C、D的总和，也就是800ms。**如果使用MQ，系统A发送数据到MQ，然后就可以返回响应给客户端，不需要再等待系统B、C、D的响应，可以大大地提高性能**。对于一些非必要的业务，比如发送短信，发送邮件等等，就可以采用MQ。

![image-20220413222934752](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413222934752.png)

​		**3.削峰**。如图所示。这其实是MQ一个很重要的应用。假设系统A在某一段时间请求数暴增，有5000个请求发送过来，系统A这时就会发送5000条SQL进入MySQL进行执行，MySQL对于如此庞大的请求当然处理不过来，MySQL就会崩溃，导致系统瘫痪。如果使用MQ，系统A不再是直接发送SQL到数据库，而是把数据发送到MQ，MQ短时间积压数据是可以接受的，然后由消费者每次拉取2000条进行处理，防止在请求峰值时期大量的请求直接发送到MySQL导致系统崩溃。

![image-20220413223005776](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413223005776.png)

**缺点：**

- 系统可用性降低 系统引入的外部依赖越多，越容易挂掉。万一 MQ 挂了，MQ 一挂，整套系统崩 溃，你不就完了？
- 系统复杂度提高 硬生生加个 MQ 进来，你怎么保证消息没有重复消费？怎么处理消息丢失的情况？
- 怎么保证消息传递的顺序性？问题一大堆。
- 一致性问题 A 系统处理完了直接返回成功了，人都以为你这个请求就成功了；但是问题是，要是 BCD 三个系统那里，BD 两个系统写库成功了，结果 C 系统写库失败了，咋整？你这数据就不一致 了。

## RabbitMQ特点

RabbitMQ是一款使用Erlang语言开发的，实现AMQP(高级消息队列协议)的开源消息中间件。首先要知道一些RabbitMQ的特点，[官网](https://www.rabbitmq.com/)可查：

1. 可靠性。支持持久化，传输确认，发布确认等保证了MQ的可靠性。
2. 灵活的分发消息策略。这应该是RabbitMQ的一大特点。在消息进入MQ前由Exchange(交换机)进行路由消息。分发消息策略有：简单模式、工作队列模式、发布订阅模式、路由模式、通配符模式。
3. 支持集群。多台RabbitMQ服务器可以组成一个集群，形成一个逻辑Broker。
4. 多种协议。RabbitMQ支持多种消息队列协议，比如 STOMP、MQTT 等等。
5. 支持多种语言客户端。RabbitMQ几乎支持所有常用编程语言，包括 Java、.NET、Ruby 等等。
6. 可视化管理界面。RabbitMQ提供了一个易用的用户界面，使得用户可以监控和管理消息 Broker。
7. 插件机制。RabbitMQ提供了许多插件，可以通过插件进行扩展，也可以编写自己的插件。

## RabbitMQ组成部分

![image-20220413225139917](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413225139917.png)

###  	Broker         

​		消息队列服务进程。此进程包括两个部分：Exchange和Queue。   

​		可以将一个RabbitMQ Broker看作一台RabbitMQ服务器。消息队列的运转过程如下：

![image-20220413225637623](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413225637623.png)

### Queue

  消息队列，存储消息的队列。

 **队列属性：**队列跟交换机共享某些属性，但是队列也有一些另外的属性。

​			Name
​			Durable（消息代理重启后，队列依旧存在）
​			Exclusive（只被一个连接（connection）使用，而且当连接关闭后队列即被删除）
​			Auto-delete（当最后一个消费者退订后即被删除）
​			Arguments（一些消息代理用他来完成类似与 TTL 的某些额外功能）

​	**队列创建：**队列在声明（declare）后才能被使用。如果一个队列尚不存在，声明一个队列会创建它。

​						如果声明的队列已经存在，并且属性完全相同，那么此次声明不会对原有队列产生任何影响。

​						如果声明中的属性与已存在队列的属性有差异，那么一个错误代码为 406 的通道级异常就会被抛出。

​	**队列持久化**：持久化队列（Durable queues）会被存储在磁盘上，当消息代理（broker）重启的时候，它依旧存在。没有被持久化的队列称作暂存队列（Transient queues）。并不是所有的场景和案例都需要将队列持久化。持久化的队列并不会使得路由到它的消息也具有持久性。倘若消息代理挂掉了，重新启动，那么在重启的过程中持久化队列会被重新声明，无论怎样，只有经过持久化的消息才能被重新恢复。

### Producer

消息生产者。生产方客户端将消息同交换机路由发送到队列中。

消息包含两部分：**消息体（payload）：**一般是一个带有业务逻辑结构的数据，比如一个JSON字符串。

​								**标签(Label)**用来描述这条消息，比如一个交换机的名称和一个路由键。

生产者把消息交由RabbitMQ，RabbitMQ之后会根据标签吧消息发送给感兴趣的消费者

### Consumer

消息消费者。消费队列中存储的消息。

​		消费者连接到RabbitMQ服务器，并订阅到队列上。当消费者消费一条消息时，只是消费消息的消息体（payload）。在消息路由的过程中，消息的标签会丢弃，存入到队列中的消息只有消息体，消费者也只会消费到消息体。

​		消息如果只是存储在队列里是没有任何用处的。被应用消费掉，消息的价值才能够体现。在 AMQP 0-9-1 模型中，有两种途径可以达到此目的：

​						将消息投递给应用 (“push API”)
​						应用根据需要主动获取消息 (“pull API”)

![image-20220413223658000](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413223658000.png)

### Exchange

​		消息队列交换机。**按一定的规则将消息路由转发到某个队列**。如果路由不到，或许返回给生产者，或许直接丢弃。message到达broker的第一站，根据分发规则，匹配查询表中的routingkey，分发消息到queue中去。常用的类型有：direct (point-to-point), topic (publish-subscribe) and fanout(multicast)

​		RabbitMQ 消息传递模型的核心思想是: **生产者生产的消息从不会直接发送到队列**。实际上，通常生产者甚至都不知道这些消息传递传递到了哪些队列中。相反，**生产者只能将消息发送到交换机****(exchange)**，交换机工作的内容非常简单，一方面它接收来自生产者的消息，另一方面将它们推入队列。交换机必须确切知道如何处理收到的消息。是应该把这些消息放到特定队列还是说把他们到许多队列中还是说应该丢弃它们。这就由交换机的类型来决定。

![image-20220414192704514](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414192704514.png)

![image-20220414192729513](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414192729513.png)

#### 无名Exchange和临时队列

​		默认交换，我们通过空字符串(“”)进行标识。

![image-20220414193531870](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414193531870.png)

​		第一个参数是交换机的名称。空字符串表示默认或无名称交换机：消息能路由发送到队列中其实是由 routingKey(bindingkey)绑定 key 指定的，如果它存在的话

​		每当我们连接到 Rabbit 时，我们都需要一个全新的空队列，为此我们可以创建一个具有**随机名称的队列**，或者能让服务器为我们选择一个随机队列名称那就更好了。其次**一旦我们断开了消费者的连接，队列将被自动删除。**

​		创建临时队列的方式如下:

​				String queueName = channel.queueDeclare().getQueue();

![image-20220414193639027](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414193639027.png)

#### 绑定(bindings)

​		什么是 bingding 呢，binding 其实是 exchange 和 queue 之间的桥梁，它告诉我们 exchange 和那个队列进行了绑定关系。比如说下面这张图告诉我们的就是 X 与 Q1 和 Q2 进行了绑定。绑定是交换机和队列之间的桥梁关系。也可以这么理解：**队列只对它绑定的交换机的消息感兴趣**。绑定用参数：routingKey来表示也可称该参数为bindingkey，创建绑定我们用代码:channel.queueBind(queueName, EXCHANGE_NAME,"routingKey");**绑定之后的意义由其交换类型决定。**

![image-20220414193752799](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414193752799.png)

#### Exchange的四种类型以及用法

##### Direct Exchange

见文知意，直连交换机意思是此交换机需要绑定一个队列，要求**该消息与一个特定的路由键完全匹配**。简单点说就是一对一的，点对点的发送。

​		如果我们希望将日志消息写入磁盘的程序仅接收严重错误(errros)，而不存储哪些警告(warning)或信息(info)日志消息避免浪费磁盘空间。Fanout这种交换类型并不能给我们带来很大的灵活性-它只能进行无意识的广播，在这里我们将使用direct这种类型来进行替换，这种类型的工作方式是，消息只去到它绑定的routingKey队列中去。

![image-20220413223824128](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413223824128.png)

###### 案例

![image-20220414211755267](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414211755267.png)

![image-20220414211805473](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414211805473.png)

**生产者**

```java
public class DirectLogs {
    //交换机名称
    public static final String EXCHANGE_NAME = "direct_logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish(EXCHANGE_NAME,"info",null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+msg);
        }
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列名称随机
         * 当消费者断开与队列的连接的时候 队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,comsumerTaf->{});
    }
}
```

**消费者**

```java
public class ReceiveLogsDirect01 {
    public static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueDeclare("console",false,false,false,null);

        channel.queueBind("console",EXCHANGE_NAME,"info");
        channel.queueBind("console",EXCHANGE_NAME,"warning");

        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume("console",true,deliverCallback,comsumerTaf->{});
    }
}
public class ReceiveLogsDirect02 {
    public static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueDeclare("disk",false,false,false,null);
        channel.queueBind("disk",EXCHANGE_NAME,"error");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect02控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume("disk",true,deliverCallback,comsumerTaf->{});
    }
}
```



##### Fanout exchange

这种类型的交换机需要将队列绑定到交换机上。**一个发送到交换机的消息都会被转发到与该交换机绑定的所有队列上**。很像子网广播，每台子网内的主机都获得了一份复制的消息。简单点说就是发布订阅。

![image-20220413223848280](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413223848280.png)

​		Fanout 这种类型非常简单。正如从名称中猜到的那样，它是将接收到的所有消息**广播**到它知道的所有队列中。系统中默认有些 exchange 类型

![image-20220414204405361](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414204405361.png)

###### 案例

生产者：

```java
public class EmitLog {
    //交换机名称
    public static final String EXCHANGE_NAME = "logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        //声明一个交换机
//        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish(EXCHANGE_NAME,"",null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+msg);
        }
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列名称随机
         * 当消费者断开与队列的连接的时候 队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,comsumerTaf->{});
    }
}
```

消费者

```java
public class ReceiveLogs01 {
    public static final String EXCHANGE_NAME = "logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列名称随机
         * 当消费者断开与队列的连接的时候 队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上");
        DeliverCallback deliverCallback = (comsumerTag,message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,comsumerTaf->{});
    }
}
public class ReceiveLogs02 {
    public static final String EXCHANGE_NAME = "logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列名称随机
         * 当消费者断开与队列的连接的时候 队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogs02控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,comsumerTaf->{});
    }
}
```

##### Topic Exchange

​		发送到类型是 topic 交换机的消息的 routing_key 不能随意写，必须满足一定的要求，它**必须是一个单词列表，以点号分隔开**。这些单词可以是任意单词，比如说："stock.usd.nyse", "nyse.vmw","quick.orange.rabbit".这种类型的。当然这个单词列表最多不能超过 255 个字节。

​		直接翻译的话叫做主题交换机，如果从用法上面翻译可能叫通配符交换机会更加贴切。这种交换机是使用通配符去匹配，路由到对应的队列。通配符有两种："*" 、 "#"。需要注意的是通配符前面必须要加上"."符号。

`*` 符号：有且只匹配一个词。比如 `a.*`可以匹配到"a.b"、"a.c"，但是匹配不了"a.b.c"。

`#` 符号：匹配一个或多个词。比如"rabbit.#"既可以匹配到"rabbit.a.b"、"rabbit.a"，也可以匹配到"rabbit.a.b.c"。

![image-20220413224112025](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413224112025.png)

###### Topic匹配案例

​		下图绑定关系如下 Q1-->绑定的是 中间带orange带3个单词的字符串(*.orange.*)

​										Q2-->绑定的是最后一个单词是rabbit的3个单词(*.*.rabbit)

​												  第一个单词是lazy的多个单词(lazy.#)

![image-20220414220559439](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414220559439.png)

 					例子的绑定关系：

​											quick.orange.rabbit  被队列 Q1Q2 接收到				quick.orange.fox  	被队列 Q1 接收到

​											lazy.orange.elephant  被队列 Q1Q2 接收到				lazy.brown.fox 		被队列 Q2 接收到

​											lazy.pink.rabbit 虽然满足两个绑定但只被队列 Q2 接收一次     quick.brown.fox   不匹配任何绑定不会被任何队列接收到会被丢弃

​											quick.orange.male.rabbit	是四个单词不匹配任何绑定会被丢弃		lazy.orange.male.rabbit	是四个单词但匹配 Q2

​				当绑定关系是下列这种情况时需要引起注意：

​							**当一个队列绑定键是#,那么这个队列将接收所有数据，就有点像fanout** ，**如果队列绑定键当中没有#和*出现，那么该队列绑定类型就是** **direct**

###### 具体案例

![image-20220414220951846](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414220951846.png)

生产者

```java
public class TopicLogs {
    public static final String EXCHANGE_NAME = "topic_logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        Map<String,String> bindingKeyMap = new HashMap<>();
        bindingKeyMap.put("quick.orange.rabbit","被队列 Q1Q2 接收到");
        bindingKeyMap.put("lazy.orange.elephant","被队列 Q1Q2 接收到");
        bindingKeyMap.put("quick.orange.fox","被队列 Q1 接收到");
        bindingKeyMap.put("lazy.brown.fox","被队列 Q2 接收到");
        bindingKeyMap.put("lazy.pink.rabbit","虽然满足两个绑定但只被队列 Q2 接收一次");
        bindingKeyMap.put("quick.brown.fox","不匹配任何绑定不会被任何队列接收到会被丢弃");
        bindingKeyMap.put("quick.orange.male.rabbit","是四个单词不匹配任何绑定会被丢弃");
        bindingKeyMap.put("lazy.orange.male.rabbit","是四个单词但匹配 Q2");
        for(Map.Entry<String,String> stringEntry:bindingKeyMap.entrySet()){
            String routingKey = stringEntry.getKey();
            String msg = stringEntry.getValue();
            channel.basicPublish(EXCHANGE_NAME,routingKey,null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息:"+msg);
        }
    }
}
```

消费者

```java
public class ReceiveTopic01 {
    public static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        //声明队列
        String quequeName = "Q1";
        channel.queueDeclare(quequeName,false,false,false,null);
        channel.queueBind(quequeName,EXCHANGE_NAME,"*.orange.*");
        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
            System.out.println("接收队列:"+quequeName+" 绑定建: "+message.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(quequeName,true,deliverCallback,comsumerTaf->{});
    }
}
public class ReceiveTopic02 {
    public static final String EXCHANGE_NAME = "topic_logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        //声明队列
        String quequeName = "Q2";
        channel.queueDeclare(quequeName,false,false,false,null);
        channel.queueBind(quequeName,EXCHANGE_NAME,"*.*.rabbit.*");
        channel.queueBind(quequeName,EXCHANGE_NAME,"lazy.#");
        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
            System.out.println("接收队列:"+quequeName+" 绑定建: "+message.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(quequeName,true,deliverCallback,comsumerTaf->{});
    }
}
```

##### Headers Exchange

这种交换机用的相对没这么多。**它跟上面三种有点区别，它的路由不是用routingKey进行路由匹配，而是在匹配请求头中所带的键值进行路由**。如图所示：

![image-20220413224518809](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413224518809.png)

创建队列需要设置绑定的头部信息，有两种模式：**全部匹配和部分匹配**。如上图所示，交换机会根据生产者发送过来的头部信息携带的键值去匹配队列绑定的键值，路由到对应的队列。

### Connection和Channel

​      connection 连接，是一条TCP连接。一旦TCP 连接建立起来，客户端紧接着可以创建一个AMQP 信道(Channel) ，每个信道都会被指派一个唯一的ID 。信道是建立在Connection 之上的**虚拟连接**， RabbitMQ 处理的每条AMQP 指令都是通过信道完成的。

​		**我们完全可以直接使用Connection 就能完成信道的工作，为什么还要引入信道呢?**

​		回答：1.一个应用程序中有很多个线程需要从RabbitMQ 中消费消息，或者生产消息，那么必然需要建立很多个Connection ，也就是许多个TCP 连接。然而对于操作系统而言，建立和销毁TCP 连接是非常昂贵的开销，如果遇到使用高峰，性能瓶颈也随之显现。RabbitMQ 采用类似NIO' (Non-blocking 1/0) 的做法，选择TCP 连接复用，不仅可以减少性能开销，同时也便于管理。

​					2.每个线程把持一个信道，所以信道复用了Connection 的TCP 连接。同时RabbitMQ 可以确保每个线程的私密性，就像拥有独立的连接一样。当每个信道的流量不是很大时，复用单一的Connection 可以在产生性能瓶颈的情况下有效地节省TCP 连接资源。但是当信道本身的流量很大时，这时候多个信道复用一个Connection 就会产生性能瓶颈，进而使整体的流量被限制了。此时就需要开辟多个Connection ，将这些信道均摊到这些Connection 中， 至于这些相关的调优策略需要根据业务自身的实际情况进行调节。

### vhosts

​		虚拟主机。为了在一个单独的代理上实现多个隔离的环境（用户、用户组、交换机、队列 等），AMQP 提供了一个虚拟主机（virtual hosts - vhosts）的概念。这跟 Web servers 虚拟主机概念非常相似，这为 AMQP 实体提供了完全隔离的环境。当连接被建立的时候，AMQP 客户端来指定使用哪个虚拟主机。

## RabbtiMQ运转流程

**生产者发送消息的时候**

(1) 生产者连接到RabbitMQ Broker ， 建立一个连接( Connection) ，开启一个信道(Channel)

(2) 生产者声明一个交换器，并设置相关属性，比如交换机类型、是否持久化等

(3) 生产者声明一个队列井设置相关属性，比如是否排他、是否持久化、是否自动删除等

( 4 ) 生产者通过路由键将交换器和队列绑定起来

( 5 ) 生产者发送消息至RabbitMQ Broker，其中包含路由键、交换器等信息

(6) 相应的交换器根据接收到的路由键查找相匹配的队列。

( 7 ) 如果找到，则将从生产者发送过来的消息存入相应的队列中。

(8) 如果没有找到，则根据生产者配置的属性选择丢弃还是回退给生产者

(9) 关闭信道。

(1 0) 关闭连接。

**消费者接收消息的过程:**

(1)消费者连接到RabbitMQ Broker ，建立一个连接(Connection ) ，开启一个信道(Channel) 。

(2) 消费者向RabbitMQ Broker 请求消费相应队列中的消息，可能会设置相应的回调函数，
以及做一些准备工作

(3)等待RabbitMQ Broker 回应并投递相应队列中的消息， 消费者接收消息。

(4) 消费者确认( ack) 接收到的消息。

( 5) RabbitMQ 从队列中删除相应己经被确认的消息。

( 6) 关闭信道。

( 7) 关闭连接



## 消息应答

​		消费者完成一个任务可能需要一段时间，如果其中一个消费者处理一个长的任务并仅只完成了部分突然它挂掉了，会发生什么情况。RabbitMQ 一旦向消费者传递了一条消息，便立即将该消息标记为删除。在这种情况下，突然有个消费者挂掉了，我们将丢失正在处理的消息。以及后续发送给该消费这的消息，因为它无法接收到。

​		为了保证消息在发送过程中不丢失，rabbitmq 引入消息应答机制，消息应答就是:**消费者在接收到消息并且处理该消息之后，告诉 rabbitmq 它已经处理了，rabbitmq 可以把该消息删除了。**

**消息应答分为手动应答和自动应答**

​		自动应答：消息发送后立即被认为已经传送成功，这种模式需要在**高吞吐量和数据传输安全性方面做权衡**,因为这种模式如果消息在接收到之前，消费者那边出现连接或者 channel 关闭，那么消息就丢失 了。当然另一方面这种模式消费者那边可以传递过载的消息，**没有对传递的消息数量进行限制**，但是这样也可能使得消费者这边由于接收太多还来不及处理的消息，导致这些消息的积压，最终使得内存耗尽，最终这些消费者线程被操作系统杀死，**所以这种模式仅适用在消费者可以高效并以某种速率能够处理这些消息的情况下使用**。

​		手动应答：Channel.basicAck(用于肯定确认) RabbitMQ 已知道该消息并且成功的处理消息，可以将其丢弃了

![image-20220414122310264](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414122310264.png)

multiple 的 true 和 false 代表不同意思。true 代表批量应答 channel 上未应答的消息， 比如说 channel 上有传送 tag 的消息 5,6,7,8 ，当前 tag 是8 ，那么此时 5-8 的这些还未应答的消息都会被确认收到消息应答。false 同上面相比只会应答 tag=8 的消息 5,6,7 这三个消息依然不会被确认收到消息应答。

​							Channel.basicNack(用于否定确认) 

​							Channel.basicReject(用于否定确认)   与 Channel.basicNack 相比少一个批量处理的参数，不处理该消息直接拒绝，可以将其丢弃

### 消息自动重新入队

​		如果消费者由于某些原因失去连接(其通道已关闭，连接已关闭或 TCP 连接丢失)，导致消息未发送 ACK 确认，RabbitMQ 将了解到消息未完全处理，并将对其重新排队。如果此时其他消费者可以处理，它将很快将其重新分发给另一个消费者。这样，即使某个消费者偶尔死亡，也可以确保不会丢失任何消息。

![image-20220414122531961](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414122531961.png)

### 代码演示

​	默认消息采用的是自动应答，所以我们要想实现消息消费过程中不丢失，需要把自动应答改为手动应答，消费者在上面代码的基础上增加下面画红色部分代码。

![image-20220414152524843](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414152524843.png)

消息生产者

```java
public class Producer02 {
    public static final String ACK_QUEUE_NAME = "ack_queue";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitUtils.getChannel();
        channel.queueDeclare(ACK_QUEUE_NAME,false,false,false,null);
        //从控制台当中接收消息
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish("",ACK_QUEUE_NAME,null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+msg);
        }
    }
}
```

 消费者

```java
public class Worker02 {
    public static final String ACK_QUEUE_NAME = "ack_queue";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        System.out.println("C1等待接收消息处理时间较短.....");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            String msg = new String(message.getBody());
            SleepUtils.sleep(1);
            System.out.println("接收到的消息："+msg);
            /**
             * 1.消息标记tag
             * 2.是否批量应答未应答消息
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
        };
        //采用手动应答
        Boolean autoAck = false;
        channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(comsumerTag->{
            System.out.println("消息者取消消费接口回调逻辑");
        }));
    }
}
public class Worker03 {
    public static final String ACK_QUEUE_NAME = "ack_queue";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        System.out.println("C2等待接收消息处理时间较长.....");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            String msg = new String(message.getBody());
            SleepUtils.sleep(30);
            System.out.println("接收到的消息："+msg);
            /**
             * 1.消息标记tag
             * 2.是否批量应答未应答消息
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
        };
        //采用手动应答
        Boolean autoAck = false;
        channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(comsumerTag->{
            System.out.println("消息者取消消费接口回调逻辑");
        }));
    }
}
```

#### 运行结果演示

​		正常情况下消息发送方发送两个消息 C1 和 C2 分别接收到消息并进行处理

![image-20220414152902603](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414152902603.png)

​		在发送者发送消息 dd，发出消息之后的把 C2 消费者停掉，按理说该 C2 来处理该消息，但是由于它处理时间较长，在还未处理完，也就是说 C2 还没有执行 ack 代码的时候，C2 被停掉了，此时会看到消息被 C1 接收到了，说明消息 dd 被重新入队，然后分配给能处理消息的 C1 处理了。

![image-20220414152946872](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414152946872.png)

![image-20220414152957659](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414152957659.png)

![image-20220414153012452](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153012452.png)

![image-20220414153023651](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153023651.png)

## RabbitMQ持久化

​		rabbitmq 如果重启的化，该队列就会被删除掉，如果要队列实现持久化 需要在声明队列的时候把 durable 参数设置为持久化

### **队列持久化**

![image-20220414153731987](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153731987.png)

​		但是需要注意的就是如果之前声明的队列不是持久化的，需要把原先队列先删除，或者重新创建一个持久化的队列，不然就会出现错误

![image-20220414153754059](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153754059.png)

![image-20220414153806806](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153806806.png)

### **消息持久化**

​		要想让消息实现持久化需要在消息生产者修改代码，MessageProperties.PERSISTENT_TEXT_PLAIN 添加这个属性。

![image-20220414153925467](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414153925467.png)

​		将消息标记为持久化并不能完全保证不会丢失消息。尽管它告诉 RabbitMQ 将消息保存到磁盘，但是这里依然存在当消息刚准备存储在磁盘的时候 但是还没有存储完，消息还在缓存的一个间隔点。此时并没有真正写入磁盘。持久性保证并不强，但是对于我们的简单任务队列而言，这已经绰绰有余了。

### 不公平分发

​		在最开始的时候我们学习到 RabbitMQ 分发消息采用的轮训分发，但是在某种场景下这种策略并不是很好，比方说有两个消费者在处理任务，其中有个消费者 1 处理任务的速度非常快，而另外一个消费者 2处理速度却很慢，这个时候我们还是采用轮训分发的化就会到这处理速度快的这个消费者很大一部分时间处于空闲状态，而处理慢的那个消费者一直在干活，这种分配方式在这种情况下其实就不太好，但是RabbitMQ 并不知道这种情况它依然很公平的进行分发。

​		**为了避免这种情况，我们可以设置参数 channel.basicQos(1);**

![image-20220414154244401](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414154244401.png)

![image-20220414154340848](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414154340848.png)

​		意思就是如果这个任务我还没有处理完或者我还没有应答你，你先别分配给我，我目前只能处理一个任务，然后rabbitmq就会把该任务分配给没有那么忙的那个空闲消费者，当然如果所有的消费者都没有完成手上任务，队列还在不停的添加新任务，队列有可能就会遇到队列被撑满的情况，这个时候就只能添加新的worker或者改变其他存储任务的策略。

### 欲取值

​		本身消息的发送就是异步发送的，所以在任何时候，channel 上肯定不止只有一个消息另外来自消费者的手动确认本质上也是异步的。因此这里就存在一个未确认的消息缓冲区，因此希望开发人员能**限制此缓冲区的大小，以避免缓冲区里面无限制的未确认消息问题**。这个时候就可以通过使用 basic.qos 方法设置“预取计数”值来完成的。**该值定义通道上允许的未确认消息的最大数量**。一旦数量达到配置的数量，RabbitMQ 将停止在通道上传递更多消息，除非至少有一个未处理的消息被确认，例如，假设在通道上有未确认的消息 5、6、7，8，并且通道的预取计数设置为 4，此时RabbitMQ 将不会在该通道上再传递任何消息，除非至少有一个未应答的消息被 ack。比方说 tag=6 这个消息刚刚被确认 ACK，RabbitMQ 将会感知这个情况到并再发送一条消息。消息应答和 QoS 预取值对用户吞吐量有重大影响。通常，增加预取将提高向消费者传递消息的速度。**虽然自动应答传输消息速率是最佳的，但是，在这种情况下已传递但尚未处理的消息的数量也会增加，从而增加了消费者的** **RAM** **消耗**(随机存取存储器)应该小心使用具有无限预处理的自动确认模式或手动确认模式，消费者消费了大量的消息如果没有确认的话，会导致消费者连接节点的内存消耗变大，所以找到合适的预取值是一个反复试验的过程，不同的负载该值取值也不同 100 到 300 范围内的值通常可提供最佳的吞吐量，并且不会给消费者带来太大的风险。预取值为 1 是最保守的。当然这将使吞吐量变得很低，特别是消费者连接延迟很严重的情况下，特别是在消费者连接等待时间较长的环境中。对于大多数应用来说，稍微高一点的值将是最佳的。

![image-20220414154805419](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414154805419.png)

## 发布与确认

​		生产者将信道设置成 confirm 模式，一旦信道进入 confirm 模式，**所有在该信道上面发布的消息都将会被指派一个唯一的 ID**(从 1 开始)，一旦消息被投递到所有匹配的队列之后，broker 就会发送一个确认给生产者(包含消息的唯一 ID)，这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会在将消息写入磁盘之后发出，broker 回传给生产者的确认消息中 delivery-tag 域包含了确认消息的序列号，此外 broker 也可以设置basic.ack 的multiple 域，表示到这个序列号之前的所有消息都已经得到了处理。

​		confirm 模式最大的好处在于他是异步的，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果 RabbitMQ 因为自身内部错误导致消息丢失，就会发送一条 nack 消息，生产者应用程序同样可以在回调方法中处理该 nack 消息。

​		发布确认默认是没有开启的，如果要开启需要调用方法 confirmSelect，每当你要想使用发布确认，都需要在 channel 上调用该方法

![image-20220414160721843](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414160721843.png)

### 单个确认发布

​		这是一种简单的确认方式，它是一种**同步确认发布**的方式，也就是发布一个消息之后只有它被确认发布，后续的消息才能继续发布,waitForConfirmsOrDie(long)这个方法只有在消息被确认的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。

​		这种确认方式有一个最大的缺点就是:**发布速度特别的慢，**因为如果没有确认发布的消息就会阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某些应用程序来说这可能已经足够了。

```java
public class ConfirmMessage {
    //批量发消息的个数
    public static final int MSG_COUNT = 1000;
    public static void main(String[] args) throws Exception{
        //1.单个确认
        ConfirmMessage.publishMessageIndividually();//发布1000个单独确认消息，耗时380ms
        //2.批量确认
        //3.异步批量确认
    }

    //单个确认
    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitUtils.getChannel();
            //队列声明
            String queueName = UUID.randomUUID().toString();
            channel.queueDeclare(queueName, true, false, false, null);
            //开启发布确认
            channel.confirmSelect();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < MSG_COUNT; i++) {
                String msg = i+"";
                channel.basicPublish("", queueName, null, msg.getBytes());
                //服务端返回 false 或超时时间内未返回，生产者可以消息重发
                boolean flag = channel.waitForConfirms();
                if(flag) {
                    System.out.println("消息发送成功");
                }
                long end = System.currentTimeMillis();
                System.out.println("发布"+MSG_COUNT+"个单独确认消息，耗时"+(end-begin)+"ms");
            }
        }
}
```

### 批量确认发布

​		与单个等待确认消息相比，先发布一批消息然后一起确认可以极大地提高吞吐量，当然这种方式的缺点就是:当发生故障导致发布出现问题时，不知道是哪个消息出现问题了，我们必须将整个批处理保存在内存中，以记录重要的信息而后重新发布消息。当然这种方案仍然是同步的，也一样阻塞消息的发布。

```java
    //批量确认  发布1000个批量确认消息，耗时34ms
public static void publishMessageBatch() throws Exception {
    Channel channel = RabbitUtils.getChannel();
    String queueName = UUID.randomUUID().toString();
    channel.queueDeclare(queueName, true, false, false, null);
    channel.confirmSelect();
    long begin = System.currentTimeMillis();
    //批量确认消息大小
    int batchSize = 100;
    for (int i = 0; i < MSG_COUNT; i++) {
        String msg = i+"";
        channel.basicPublish("", queueName, null, msg.getBytes());
        //判断达到100条消息的时候，批量确认一次
        if(i%batchSize==0) {
            channel.waitForConfirms();
        }
    }
    long end = System.currentTimeMillis();
    System.out.println("发布"+MSG_COUNT+"个批量确认消息，耗时"+(end-begin)+"ms");
}
```

### 异步确认发布

​		异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都没得说，它是利用回调函数来达到消息可靠性传递的，这个中间件也是通过函数回调来保证是否投递成功

![image-20220414171313095](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414171313095.png)

```java
//异步发布确认  发布1000个异步确认消息，耗时29ms
public static void publishMessageAsync()throws Exception{
    Channel channel = RabbitUtils.getChannel();
    String queueName = UUID.randomUUID().toString();
    channel.queueDeclare(queueName, true, false, false, null);
    channel.confirmSelect();
    long begin = System.currentTimeMillis();
    //消息确认成功  回调函数
    ConfirmCallback ackCallback = (deliveryTag,multiple)->{
        System.out.println("确认消息："+deliveryTag);
    };
    //消息确认失败  回调函数   1.消息标记 2.是否为批量确认
    ConfirmCallback nackCallback = (deliveryTag,multiple)->{
        System.out.println("未确认消息："+deliveryTag);
    };
    //准备消息的监听器 1 监听哪些消息成功了 2哪些消息失败了
    channel.addConfirmListener(ackCallback,nackCallback);
    for (int i = 0; i < MSG_COUNT; i++) {
        String msg = i+"";
        channel.basicPublish("", queueName, null, msg.getBytes());
    }
    long end = System.currentTimeMillis();
    System.out.println("发布"+MSG_COUNT+"个异步确认消息，耗时"+(end-begin)+"ms");
}
```

### 如何处理异步未确认消息？

​		最好的解决的解决方案就是把未确认的消息放到一个基于内存的能被发布线程访问的队列，比如说用 ConcurrentLinkedQueue 这个队列在 confirm callbacks 与发布线程之间进行消息的传递。

```java
   //批量确认  发布1000个批量确认消息，耗时34ms
public static void publishMessageBatch() throws Exception {
    Channel channel = RabbitUtils.getChannel();
    String queueName = UUID.randomUUID().toString();
    channel.queueDeclare(queueName, true, false, false, null);
    channel.confirmSelect();
    long begin = System.currentTimeMillis();
    //批量确认消息大小
    int batchSize = 100;
    for (int i = 0; i < MSG_COUNT; i++) {
        String msg = i+"";
        channel.basicPublish("", queueName, null, msg.getBytes());
        //判断达到100条消息的时候，批量确认一次
        if(i%batchSize==0) {
            channel.waitForConfirms();
        }
    }
    long end = System.currentTimeMillis();
    System.out.println("发布"+MSG_COUNT+"个批量确认消息，耗时"+(end-begin)+"ms");
}
//异步发布确认  发布1000个异步确认消息，耗时29ms
public static void publishMessageAsync()throws Exception{
    Channel channel = RabbitUtils.getChannel();
    String queueName = UUID.randomUUID().toString();
    channel.queueDeclare(queueName, true, false, false, null);
    channel.confirmSelect();
    long begin = System.currentTimeMillis();
    /**
     * 线程安全有序的哈希表 适用于高并发的情况
     * 1.可以将序号与消息进行关联
     * 2.可以批量删除消息 只需要知道序号
     * 3.支持高并发（多线程)
     */
    ConcurrentSkipListMap<Long,String> outstandingConfirms =
            new ConcurrentSkipListMap<>();
    //消息确认成功  回调函数
    ConfirmCallback ackCallback = (deliveryTag,multiple)->{
        if(multiple){
            // 2*.删除掉已经确认的消息 剩下的就是未确认的消息
            ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(deliveryTag);
            confirmed.clear();
        }else{
            outstandingConfirms.remove(deliveryTag);
        }
        System.out.println("确认消息："+deliveryTag);
    };
    //消息确认失败  回调函数   1.消息标记 2.是否为批量确认
    ConfirmCallback nackCallback = (deliveryTag,multiple)->{
        // 3*.打印一下未确认的消息都有哪些
        String msg = outstandingConfirms.get(deliveryTag);
        System.out.println("未确认的消息是："+msg+"::::未确认的消息tag:"+deliveryTag);
    };
    //准备消息的监听器 1 监听哪些消息成功了 2哪些消息失败了
    channel.addConfirmListener(ackCallback,nackCallback);
    for (int i = 0; i < MSG_COUNT; i++) {
        String msg = i+"";
        channel.basicPublish("", queueName, null, msg.getBytes());
        // 1*.此处记录下所有要发送的消息   消息的总和
        outstandingConfirms.put(channel.getNextPublishSeqNo(),msg);
    }
    long end = System.currentTimeMillis();
    System.out.println("发布"+MSG_COUNT+"个异步确认消息，耗时"+(end-begin)+"ms");
}
```

## AMQP协议

RabbitMQ是采用Erlang语言实现AMQP（Advanced Message Queuing Protocol,高级消息队列协议）的消息中间件。

AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。

AMQP 说到底还是一个通信协议，通信协议都会涉及报文交互，从low-level 举例来说，AMQP 本身是应用层的协议，其填充于TCP 协议层的数据部分。而从high-level 来说， AMQP是通过协议命令进行交互的。AMQP 协议可以看作一系列结构化命令的集合，这里的命令代表一种操作，类似于HTTP 中的方法(GET 、POST 、PUT 、DELETE 等) 

**三层协议：**

​		**Module Layer**:协议最高层，主要定义了一些客户端调用的命令，客户端可以用这些命令实现自己的业务逻辑。

​		**Session Layer**:中间层，主要负责客户端命令发送给服务器，再将服务端应答返回客户端，提供可靠性同步机制和错误处理。

​		**TransportLayer**:最底层，主要传输二进制数据流，提供帧的处理、信道服用、错误检测和数据表示等。

**几大组件：**

- 交换器 (Exchange)：消息代理服务器中用于把消息路由到队列的组件。
- 队列 (Queue)：用来存储消息的数据结构，位于硬盘或内存中。
- 绑定 (Binding)：一套规则，告知交换器消息应该将消息投递给哪个队列。

### 消息机制

#### 消息确认

消费者应用（Consumer applications） - 用来接受和处理消息的应用 - 在处理消息的时候偶尔会失败或者有时会直接崩溃掉。而且网络原因也有可能引起各种问题。这就给我们出了个难题，AMQP 代理在什么时候删除消息才是正确的？AMQP 0-9-1 规范给我们两种建议：

自动确认模式：当消息代理（broker）将消息发送给应用后立即删除。（使用 AMQP 方法：basic.deliver 或 basic.get-ok）)
显式确认模式：待应用（application）发送一个确认回执（acknowledgement）后再删除消息。（使用 AMQP 方法：basic.ack）
如果一个消费者在尚未发送确认回执的情况下挂掉了，那 AMQP 代理会将消息重新投递给另一个消费者。如果当时没有可用的消费者了，消息代理会死等下一个注册到此队列的消费者，然后再次尝试投递。

##### 拒绝消息

当一个消费者接收到某条消息后，处理过程有可能成功，有可能失败。应用可以向消息代理表明，本条消息由于 “拒绝消息（Rejecting Messages）” 的原因处理失败了（或者未能在此时完成）。

当拒绝某条消息时，应用可以告诉消息代理如何处理这条消息——销毁它或者重新放入队列。

当此队列只有一个消费者时，请确认不要由于拒绝消息并且选择了重新放入队列的行为而引起消息在同一个消费者身上无限循环的情况发生。

在 AMQP 中，basic.reject 方法用来执行拒绝消息的操作。但 basic.reject 有个限制：你不能使用它决绝多个带有确认回执（acknowledgements）的消息。但是如果你使用的是 RabbitMQ，那么你可以使用被称作 negative acknowledgements（也叫 nacks）的 AMQP 0-9-1 扩展来解决这个问题。

##### 预取消息

在多个消费者共享一个队列的案例中，明确指定在收到下一个确认回执前每个消费者一次可以接受多少条消息是非常有用的。这可以在试图批量发布消息的时候起到简单的负载均衡和提高消息吞吐量的作用。For example, if a producing application sends messages every minute because of the nature of the work it is doing.（例如，如果生产应用每分钟才发送一条消息，这说明处理工作尚在运行。）

注意，RabbitMQ 只支持通道级的预取计数，而不是连接级的或者基于大小的预取。

##### 消息属性

AMQP 模型中的消息（Message）对象是带有属性（Attributes）的。有些属性及其常见，以至于 AMQP 0-9-1 明确的定义了它们，并且应用开发者们无需费心思思考这些属性名字所代表的具体含义。例如：

Content type（内容类型）
Content encoding（内容编码）
Routing key（路由键）
Delivery mode (persistent or not)
投递模式（持久化 或 非持久化）
Message priority（消息优先权）
Message publishing timestamp（消息发布的时间戳）
Expiration period（消息有效期）
Publisher application id（发布应用的 ID）
有些属性是被 AMQP 代理所使用的，但是大多数是开放给接收它们的应用解释器用的。有些属性是可选的也被称作消息头（headers）。他们跟 HTTP 协议的 X-Headers 很相似。消息属性需要在消息被发布的时候定义。

##### 消息主体

AMQP 的消息除属性外，也含有一个有效载荷 - Payload（消息实际携带的数据），它被 AMQP 代理当作不透明的字节数组来对待。

消息代理不会检查或者修改有效载荷。消息可以只包含属性而不携带有效载荷。它通常会使用类似 JSON 这种序列化的格式数据，为了节省，协议缓冲器和 MessagePack 将结构化数据序列化，以便以消息的有效载荷的形式发布。AMQP 及其同行者们通常使用 “content-type” 和 “content-encoding” 这两个字段来与消息沟通进行有效载荷的辨识工作，但这仅仅是基于约定而已。

##### 消息持久化

消息能够以持久化的方式发布，AMQP 代理会将此消息存储在磁盘上。如果服务器重启，系统会确认收到的持久化消息未丢失。

简单地将消息发送给一个持久化的交换机或者路由给一个持久化的队列，并不会使得此消息具有持久化性质：它完全取决与消息本身的持久模式（persistence mode）。将消息以持久化方式发布时，会对性能造成一定的影响（就像数据库操作一样，健壮性的存在必定造成一些性能牺牲）。

## 死信队列

​		先从概念解释上搞清楚这个定义，死信，顾名思义就是无法被消费的消息，字面意思可以这样理解，一般来说，producer 将消息投递到 broker 或者直接到queue 里了，consumer 从 queue 取出消息进行消费，但某些时候由于特定的**原因导致** **queue** **中的某些消息无法被消费**，这样的消息如果没有后续的处理，就变成了死信，有死信自然就有了死信队列。

​		应用场景:为了保证订单业务的消息数据不丢失，需要使用到 RabbitMQ 的死信队列机制，当消息消费发生异常时，将消息投入死信队列中.还有比如说: 用户在商城下单成功并点击去支付后在指定时间未支付时自动失效

​		**死信来源：**1.消息 TTL 过期，2.队列达到最大长度(队列满了，无法再添加数据到 mq 中)，3.消息被拒绝(basic.reject 或 basic.nack)并且 requeue=false.

![image-20220415113735133](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220415113735133.png)

### 案例

生产者

```java
public class Producer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        //死信消息 设置TTL时间 单位是ms  10000ms=10s
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("10000").build();
        for (int i=1;i<11;i++){
            String msg = "info" + i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",properties,msg.getBytes());
            System.out.println("生产者发送消息"+msg);
        }
    }
}
```

消费者

```java
public class Consumer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    //死信交换机
    public static final String DEAD_EXCHANGE = "dead_exchange";
    //普通队列名称
    public static final String NORMAL_QUEUE = "normal_queue";
    //死信队列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        //声明死信和普通交换机 类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE,BuiltinExchangeType.DIRECT);
        //声明普通队列
        Map<String,Object> arguments = new HashMap<>();
        //过期时间 10s=10000ms
//        arguments.put("x-message-ttl",10000);
        //正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        //设置正常队列长度的限制
//        arguments.put("x-max-length",6);
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);
        //声明死信队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);
        //绑定普通交换机与普通队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        //绑定死信的交换机与死信的队列
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");
        System.out.println("等待接收消息......");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            String msg=new String(message.getBody(),"UTF-8");
            if(msg.equals("info5")){
                System.out.println("Consumer01接收的消息是:"+msg+":此消息是被C1拒绝的");
                channel.basicReject(message.getEnvelope().getDeliveryTag(),false);
            }else{
                System.out.println("Consumer01控制台打印接收到的消息："+msg);
                channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            }
        };
        channel.basicConsume(NORMAL_QUEUE,false,deliverCallback,comsumerTag->{});
    }
}
public class Consumer02 {
    //死信队列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        System.out.println("等待接收消息......");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("Consumer02控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,comsumerTag->{});
    }
}
```

## 延迟队列

​		延时队列,队列内部是有序的，最重要的特性就体现在它的延时属性上，延时队列中的元素是希望在指定时间到了以后或之前取出和处理，简单来说，延时队列就是用来存放需要在指定时间被处理的元素的队列。

​		**延迟队列使用场景：**1.订单在十分钟之内未支付则自动取消	2.新创建的店铺，如果在十天内都没有上传过商品，则自动发送消息提醒。

​											3.用户注册成功后，如果三天内没有登陆则进行短信提醒。		4.用户发起退款，如果三天内没有得到处理则通知相关运营人员。

​											5.预定会议后，需要在预定的时间点前十分钟通知各个与会人员参加会议

​		**TTL：**TTL 是 RabbitMQ 中一个消息或者队列的属性，表明一条消息或者该队列中的所有消息的最大存活时间，单位是毫秒。换句话说，如果一条消息设置了 TTL 属性或者进入了设置TTL 属性的队列，那么这条消息如果在TTL 设置的时间内没有被消费，则会成为"死信"。如果同时配置了队列的TTL 和消息的TTL，那么较小的那个值将会被使用，有两种方式设置 TTL。

​						消息设置TTL：

![image-20220415153646860](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220415153646860.png)

​						队列设置TTL：创建队列的时候设置队列的“x-message-ttl”属性

![image-20220415153712328](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220415153712328.png)

​					队列设置TTL和消息设置TTL的区别：如果设置了队列的 TTL 属性，那么一旦消息过期，就会被队列丢弃(如果配置了死信队列被丢到死信队列中)，而第二种方式，消息即使过期，也不一定会被马上丢弃，因为**消息是否过期是在即将投递到消费者之前判定的**，如果当前队列有严重的消息积压情况，则已过期的消息也许还能存活较长时间；另外，还需要注意的一点是，如果不设置 TTL，表示消息永远不会过期，如果将 TTL 设置为 0，则表示除非此时可以直接投递该消息到消费者，否则该消息将会被丢弃。

### 案例

​		创建两个队列 QA 和 QB，两者队列 TTL 分别设置为 10S 和 40S，然后在创建一个交换机 X 和死信交换机 Y，它们的类型都是direct，创建一个死信队列 QD，还有队列 QC,该队列不设置TTL 时间它们的绑定关系如下：

![image-20220416140953758](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416140953758.png)



配置类

```java
package com.oddfar.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class TtlQueueConfig {
    public static final String X_EXCHANGE = "X";
    public static final String QUEUE_A = "QA";
    public static final String QUEUE_B = "QB";
    //死信交换机
    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    //死信队列
    public static final String DEAD_LETTER_QUEUE = "QD";

    // 声明 xExchange
    @Bean("xExchange")
    public DirectExchange xExchange() {
        return new DirectExchange(X_EXCHANGE);
    }

    // 声明 死信队列交换机
    @Bean("yExchange")
    public DirectExchange yExchange() {
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    //声明队列 A ttl 为 10s 并绑定到对应的死信交换机
    @Bean("queueA")
    public Queue queueA() {
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //声明队列的 TTL
        args.put("x-message-ttl", 10000);
        return QueueBuilder.durable(QUEUE_A).withArguments(args).build();
    }

    // 声明队列 A 绑定 X 交换机
    @Bean
    public Binding queueaBindingX(@Qualifier("queueA") Queue queueA,
                                  @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueA).to(xExchange).with("XA");
    }

    //声明队列 B ttl 为 40s 并绑定到对应的死信交换机
    @Bean("queueB")
    public Queue queueB() {
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //声明队列的 TTL
        args.put("x-message-ttl", 40000);
        return QueueBuilder.durable(QUEUE_B).withArguments(args).build();
    }

    //声明队列 B 绑定 X 交换机
    @Bean
    public Binding queuebBindingX(@Qualifier("queueB") Queue queue1B,
                                  @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queue1B).to(xExchange).with("XB");
    }

    //声明死信队列 QD
    @Bean("queueD")
    public Queue queueD() {
        return new Queue(DEAD_LETTER_QUEUE);
    }

    //声明死信队列 QD 绑定关系
    @Bean
    public Binding deadLetterBindingQAD(@Qualifier("queueD") Queue queueD,
                                        @Qualifier("yExchange") DirectExchange yExchange) {
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }

}

```

生产者

```java
@Slf4j
@RequestMapping("ttl")
@RestController
public class SendMsgController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMsg/{message}")
    public void sendMsg(@PathVariable String message) {
        log.info("当前时间：{},发送一条信息给两个 TTL 队列:{}", new Date(), message);
        rabbitTemplate.convertAndSend("X", "XA", "消息来自 ttl 为 10S 的队列: " + message);
        rabbitTemplate.convertAndSend("X", "XB", "消息来自 ttl 为 40S 的队列: " + message);
    }
    
}
```

消费者

```java
@Slf4j
@Component
public class DeadLettterQueueConsumer {
    @RabbitListener(queues = "QD")
    public void receiveD(Message message, Channel channel)throws IOException{
        String msg = new String(message.getBody());
        log.info("当前时间:{},收到死信队列信息{}",new Date().toString(),msg);
    }
}
```

发起一个请求 http://localhost:8080/ttl/sendMsg/嘻嘻嘻

![image-20220416141106776](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141106776.png)

​		第一条消息在 10S 后变成了死信消息，然后被消费者消费掉，第二条消息在 40S 之后变成了死信消息， 然后被消费掉，这样一个延时队列就打造完成了。

​		不过，如果这样使用的话，岂不是每增加一个新的时间需求，就要新增一个队列，这里只有 10S 和 40S 两个时间选项，如果需要一个小时后处理，那么就需要增加TTL 为一个小时的队列，如果是预定会议室然后提前通知这样的场景，岂不是要增加无数个队列才能满足需求？

### 案例优化

​		在这里新增了一个队列 QC,绑定关系如下,该队列不设置TTL 时间

![image-20220416141158057](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141158057.png)

配置类

```java
@Configuration
public class MsgTtlQueueConfig {
    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    public static final String QUEUE_C = "QC";

    //声明队列 C 死信交换机
    @Bean("queueC")
    public Queue queueB() {
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //没有声明 TTL 属性
        return QueueBuilder.durable(QUEUE_C).withArguments(args).build();
    }

    //声明队列 B 绑定 X 交换机
    @Bean
    public Binding queuecBindingX(@Qualifier("queueC") Queue queueC,
                                  @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueC).to(xExchange).with("XC");
    }
}
```

生产者

```java
/**
 * 延时队列优化
 * @param message 消息
 * @param ttlTime 延时的毫秒
 */
@GetMapping("sendExpirationMsg/{message}/{ttlTime}")
public void sendMsg(@PathVariable String message, @PathVariable String ttlTime) {
    rabbitTemplate.convertAndSend("X", "XC", message, correlationData -> {
        correlationData.getMessageProperties().setExpiration(ttlTime);
        return correlationData;
    });
    log.info("当前时间：{},发送一条时长{}毫秒 TTL 信息给队列 C:{}", new Date(), ttlTime, message);
}
```

发起请求

​			[http://localhost:8080/ttl/sendExpirationMsg/你好1/20000](http://localhost:8080/ttl/sendExpirationMsg/你好1/20000)

​			http://localhost:8080/ttl/sendExpirationMsg/你好2/2000

![image-20220416141307919](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141307919.png)

​		看起来似乎没什么问题，但是在最开始的时候，就介绍过如果使用在消息属性上设置 TTL 的方式，消息可能并不会按时“死亡“

​		因为 RabbitMQ 只会检查第一个消息是否过期，如果过期则丢到死信队列， 如果第一个消息的延时时长很长，而第二个消息的延时时长很短，第二个消息并不会优先得到执行。这也就是为什么第二个延时2秒，却后执行。

### 使用插件案例

​			可去[官网下载 (opens new window)](https://www.rabbitmq.com/community-plugins.html)**rabbitmq_delayed_message_exchange** 插件，放置到 RabbitMQ 的插件目录。

​		进入 RabbitMQ 的安装目录下的 plgins 目录，执行下面命令让该插件生效，然后重启 RabbitMQ

```sh
[root@VM-0-6-centos software]# ls
erlang-21.3.8.21-1.el7.x86_64.rpm  rabbitmq_delayed_message_exchange-3.8.0.ez  rabbitmq-server-3.8.8-1.el7.noarch.rpm
#移动
cp rabbitmq_delayed_message_exchange-3.8.0.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.8.8/plugins
#安装
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
#重启服务
systemctl restart rabbitmq-server
```

![image-20220416141425205](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141425205.png)

​		在这里新增了一个队列delayed.queue,一个自定义交换机 delayed.exchange，绑定关系如下:

![image-20220416141447013](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141447013.png)

配置类

​		在我们自定义的交换机中，这是一种新的交换类型，该类型消息支持延迟投递机制消息传递后并不会立即投递到目标队列中，而是存储在 mnesia(一个分布式数据系统)表中，当达到投递时间时，才投递到目标队列中。

```java
@Configuration
public class DelayedQueueConfig {
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";
    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
    public static final String DELAYED_ROUTING_KEY = "delayed.routingkey";

    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAYED_QUEUE_NAME);
    }

    //自定义交换机 我们在这里定义的是一个延迟交换机
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        //自定义交换机的类型
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding bindingDelayedQueue(@Qualifier("delayedQueue") Queue queue,
                                       @Qualifier("delayedExchange") CustomExchange delayedExchange) {
        return BindingBuilder.bind(queue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }

}
```

生产者

```java
@GetMapping("sendDelayMsg/{message}/{delayTime}")
public void sendMsg(@PathVariable String message, @PathVariable Integer delayTime) {
    rabbitTemplate.convertAndSend(DELAYED_EXCHANGE_NAME, DELAYED_ROUTING_KEY, message,
            correlationData -> {
                correlationData.getMessageProperties().setDelay(delayTime);
                return correlationData;
            });
    log.info(" 当 前 时 间 ： {}, 发 送 一 条 延 迟 {} 毫秒的信息给队列 delayed.queue:{}", new Date(), delayTime, message);
}
```

消费者

```java
/**
 * 消费者 - 基于插件的延时队列
 *
 * @author zhiyuan
 */
@Slf4j
@ComponentScan
public class DelayQueueConsumer {

    public static final String DELAYED_QUEUE_NAME = "delayed.queue";

    @RabbitListener(queues = DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message) {
        String msg = new String(message.getBody());
        log.info("当前时间：{},收到延时队列的消息：{}", new Date().toString(), msg);
    }
}
```

发起请求

- [http://localhost:8080/ttl/sendDelayMsg/hello1/20000(opens new window)](http://localhost:8080/ttl/sendDelayMsg/hello1/20000)
- http://localhost:8080/ttl/sendDelayMsg/hello2/2000

![image-20220416141544139](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416141544139.png)

第二个消息被先消费掉了，符合预期

### 总结

​		延时队列在需要延时处理的场景下非常有用，使用 RabbitMQ 来实现延时队列可以很好的利用 RabbitMQ 的特性，如：消息可靠发送、消息可靠投递、死信队列来保障消息至少被消费一次以及未被正确处理的消息不会被丢弃。另外，通过 RabbitMQ 集群的特性，可以很好的解决单点故障问题，不会因为 单个节点挂掉导致延时队列不可用或者消息丢失。

​		当然，延时队列还有很多其它选择，比如利用 Java 的 DelayQueue，利用 Redis 的 zset，利用 Quartz 或者利用 kafka 的时间轮，这些方式各有特点,看需要适用的场景

# 常问问题

### Kafka、ActiveMQ、[RabbitMQ](https://so.csdn.net/so/search?q=RabbitMQ&spm=1001.2101.3001.7020)、RocketMQ 有什么优缺点？

![image-20220413232002595](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413232002595.png)

综上，各种对比之后，有如下建议：

一般的业务系统要引入 MQ，最早大家都用 ActiveMQ，但是现在确实大家用的不多了，没经过大规模吞吐量场景的验证，社区也不是很活跃，所以大家还是算了吧，我个人不推荐用这个了；

后来大家开始用 RabbitMQ，但是确实 erlang 语言阻止了大量的 Java 工程师去深入研究和掌控它，对公司而言，几乎处于不可控的状态，但是确实人家是开源的，比较稳定的支持，活跃度也高；

不过现在确实越来越多的公司会去用 RocketMQ，确实很不错，毕竟是阿里出品，但社区可能有突然黄掉的风险（目前 RocketMQ 已捐给 Apache，但 GitHub 上的活跃度其实不算高）对自己公司技术实力有绝对自信的，推荐用 RocketMQ，否则回去老老实实用 RabbitMQ 吧，人家有活跃的开源社区，绝对不会黄。

所以中小型公司，技术实力较为一般，技术挑战不是特别高，用 RabbitMQ 是不错的选择；大型公司，基础架构研发实力较强，用 RocketMQ 是很好的选择。

如果是大数据领域的实时计算、日志采集等场景，用 Kafka 是业内标准的，绝对没问题，社区活跃度很高，绝对不会黄，何况几乎是全世界这个领域的事实性规范。

### MQ 有哪些常见问题？如何解决这些问题？

​		**消息的顺序问题**

​					消息有序指的是可以按照消息的发送顺序来消费。假如生产者产生了 2 条消息：M1、M2，假定 M1 发送到 S1，M2 发送到 S2，如果要保证 M1 先于 M2 被消费，怎么做？

​					解决方案：保证生产者 - MQServer - 消费者是一对一对一的关系

![image-20220413232535281](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220413232535281.png)

​					缺陷：并行度就会成为消息系统的瓶颈（吞吐量不够）

​								更多的异常处理，比如：只要消费端出现问题，就会导致整个处理流程阻塞，我们不得不花费更多的精力来解决阻塞的问题。

​								队列无序并不意味着消息无序 所以从业务层面来保证消息的顺序而不仅仅是依赖于消息系统，是一种更合理的方式。

​		**消息的重复问题**

​					造成消息重复的根本原因是：网络不可达。所以解决这个问题的办法就是绕过这个问题。那么问题就变成了：如果消费端收到两条一样的消息，应该怎样处理？消费端处理消息的业务逻辑保持幂等性。只要保持幂等性，不管来多少条重复消息，最后处理的结果都一样。保证每条消息都有唯一编号且保证消息处理成功与去重表的日志同时出现。利用一张日志表来记录已经处理成功的消息的 ID，如果新到的消息 ID 已经在日志表中，那么就不再处理这条消息

### 如何保证RabbitMQ消息的顺序性和可靠性？

**顺序性**

​			拆分多个 queue(消息队列)，每个 queue(消息队列) 一个 consumer(消费者)，就是多一些 queue (消息队列)而已，确实是麻烦点；

​			或者就一个 queue (消息队列)但是对应一个 consumer(消费者)，然后这个 consumer(消费者)内部用内存队列做排队，然后分发给底层不同的 worker 来处理。

**可靠性**

​		消息到MQ的过程中搞丢，MQ自己搞丢，MQ到消费过程中搞丢。

​		`生产者到RabbitMQ`：事务机制和Confirm机制，注意：事务机制和 Confirm 机制是互斥的，两者不能共存，会导致 RabbitMQ 报错。

​		`RabbitMQ自身`：持久化、集群、普通模式、镜像模式。

​		`RabbitMQ到消费者`：basicAck机制、死信队列、消息补偿机制。

### 消息如何分发？

- 若该队列至少有一个消费者订阅，消息将以循环（round-robin）的方式发送给消费者。每条消息只会分发给一个订阅的消费者（前提是消费者能够正常处理消息并进行确认）。通过路由可实现多消费的功能

### 消息怎么路由？

- 消息提供方->路由->一至多个队列消息发布到交换器时，消息将拥有一个路由键（routing key），在消息创建时设定。通过队列路由键，可以把队列绑定到交换器上。消息到达交换器后，RabbitMQ 会将消息的路由键与队列的路由键进行匹配（针对不同的交换器有不同的路由规则）；

**常用的交换器主要分为一下三种：**

1. fanout：如果交换器收到消息，将会广播到所有绑定的队列上
2. direct：如果路由键完全匹配，消息就被投递到相应的队列
3. topic：可以使来自不同源头的消息能够到达同一个队列。 使用 topic 交换器时，可以使用通配符

消息怎么路由？

### 如何保证消息消费时的幂等性？

​		先说为什么会重复消费：正常情况下，消费者在消费消息的时候，消费完毕后，会发送一个确认消息给消息队列，消息队列就知道该消息被消费了，就会将该消息从消息队列中删除；

​		但是因为网络传输等等故障，确认信息没有传送到消息队列，导致消息队列不知道自己已经消费过该消息了，再次将消息分发给其他的消费者。

​		针对以上问题，一个解决思路是：保证消息的唯一性，就算是多次传输，不要让消息的多次消费带来影响；保证消息等幂性；

​		比如：在写入消息队列的数据做唯一标示，消费消息时，根据唯一标识判断是否消费过；

​		假设你有个系统，消费一条消息就往数据库里插入一条数据，要是你一个消息重复两次，你不就插入了两条，这数据不就错了？但是你要是消费到第二次的时候，自己判断一下是否已经消费过了，若是就直接扔了，这样不就保留了一条数据，从而保证了数据的正确性。

### 如何确保消息正确地发送至 RabbitMQ？ 如何确保消息接收方消费了消息？

**发送方确认模式**
		将信道设置成 confirm 模式（发送方确认模式），则所有在信道上发布的消息都会被指派一个唯一的 ID。

​		一旦消息被投递到目的队列后，或者消息被写入磁盘后（可持久化的消息），信道会发送一个确认给生产者（包含消息唯一 ID）。

​		如果 RabbitMQ 发生内部错误从而导致消息丢失，会发送一条 nack（notacknowledged，未确认）消息。

​		发送方确认模式是异步的，生产者应用程序在等待确认的同时，可以继续发送消息。当确认消息到达生产者应用程序，生产者应用程序的回调方法就会被触发来处理确认消息。

**接收方确认机制**
		消费者接收每一条消息后都必须进行确认（消息接收和消息确认是两个不同操作）。只有消费者确认了消息，RabbitMQ 才能安全地把消息从队列中删除。

​		这里并没有用到超时机制，RabbitMQ 仅通过 Consumer 的连接中断来确认是否需要重新发送消息。也就是说，只要连接不中断，RabbitMQ 给了 Consumer 足够长的时间来处理消息。保证数据的最终一致性；

​		**下面罗列几种特殊情况**
如果消费者接收到消息，在确认之前断开了连接或取消订阅，RabbitMQ 会认为消息没有被分发，然后重新分发给下一个订阅的消费者。（可能存在消息重复消费的隐患，需要去重）
如果消费者接收到消息却没有确认消息，连接也未断开，则 RabbitMQ 认为该消费者繁忙，将不会给该消费者分发更多的消息。

### 如何保证高可用的？RabbitMQ 的集群

- RabbitMQ 是比较有代表性的，因为是基于主从（非分布式）做高可用性的，我们就以 RabbitMQ 为例子讲解第一种 MQ 的高可用性怎么实现。RabbitMQ 有三种模式：单机模式、普通集群模式、镜像集群模式。

**单机模式**
		单机模式，就是 Demo 级别的，一般就是你本地启动了玩玩儿的?，没人生产用单机模式
**普通集群模式**
		意思就是在多台机器上启动多个 RabbitMQ 实例，每个机器启动一个。
		你创建的 queue，只会放在一个 RabbitMQ 实例上，但是每个实例都同步 queue 的元数据（元数据可以认为是 queue 的一些配置信息，通过元数据，可以找到 queue 所在实例）。你消费的时候，实际上如果连接到了另外一个实例，那么那个实例会从 queue 所在实例上拉取数据过来。这方案主要是提高吞吐量的，就是说让集群中多个节点来服务某个 queue 的读写操作。
**镜像集群模式**

​		这种模式，才是所谓的 RabbitMQ 的高可用模式。跟普通集群模式不一样的是，在镜像集群模式下，你创建的 queue，无论元数据还是 queue 里的消息都会存在于多个实例上，就是说，每个 RabbitMQ 节点都有这个 queue 的一个完整镜像，包含 queue 的全部数据的意思。然后每次你写消息到 queue 的时候，都会自动把消息同步到多个实例的 queue 上。RabbitMQ 有很好的管理控制台，就是在后台新增一个策略，这个策略是镜像集群模式的策略，指定的时候是可以要求数据同步到所有节点的，也可以要求同步到指定数量的节点，再次创建 queue 的时候，应用这个策略，就会自动将数据同步到其他的节点上去了。

这样的好处在于，你任何一个机器宕机了，没事儿，其它机器（节点）还包含了这个 queue 的完整数据，别的 consumer 都可以到其它节点上去消费数据。坏处在于，第一，这个性能开销也太大了吧，消息需要同步到所有机器上，导致网络带宽压力和消耗很重！RabbitMQ 一个 queue 的数据都是放在一个节点里的，镜像集群下，也是每个节点都放这个 queue 的完整数据。

### 如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时，说说怎么解决？

​		1.消息积压处理办法：临时紧急扩容：

​		2.先修复 consumer 的问题，确保其恢复消费速度，然后将现有 cnosumer 都停掉。新建一个 topic，partition 是原来的 10 倍，临时建立好原先 10 倍的 queue 数量。然后写一个临时的分发数据的 consumer 程序，这个程序部署上去消费积压的数据，消费之后不做耗时的处理，直接均匀轮询写入临时建立好的 10 倍数量的 queue。
​		接着临时征用 10 倍的机器来部署 consumer，每一批 consumer 消费一个临时 queue 的数据。这种做法相当于是临时将 queue 资源和 consumer 资源扩大 10 倍，以正常的 10 倍速度来消费数据。
​		等快速消费完积压数据之后，得恢复原先部署的架构，重新用原先的 consumer 机器来消费消息。
​		MQ中消息失效：假设你用的是 RabbitMQ，RabbtiMQ 是可以设置过期时间的，也就是 TTL。如果消息在 queue 中积压超过一定的时间就会被 RabbitMQ 给清理掉，这个数据就没了。那这就是第二个坑了。这就不是说数据会大量积压在 mq 里，而是大量的数据会直接搞丢。我们可以采取一个方案，就是批量重导，这个我们之前线上也有类似的场景干过。就是大量积压的时候，我们当时就直接丢弃数据了，然后等过了高峰期以后，比如大家一起喝咖啡熬夜到晚上12点以后，用户都睡觉了。这个时候我们就开始写程序，将丢失的那批数据，写个临时程序，一点一点的查出来，然后重新灌入 mq 里面去，把白天丢的数据给他补回来。也只能是这样了。假设 1 万个订单积压在 mq 里面，没有处理，其中 1000 个订单都丢了，你只能手动写程序把那 1000 个订单给查出来，手动发到 mq 里去再补一次。

​		mq消息队列块满了：如果消息积压在 mq 里，你很长时间都没有处理掉，此时导致 mq 都快写满了，咋办？这个还有别的办法吗？没有，谁让你第一个方案执行的太慢了，你临时写程序，接入数据来消费，消费一个丢弃一个，都不要了，快速消费掉所有的消息。然后走第二个方案，到了晚上再补数据吧。

### 消息基于什么传输？

​		由于TCP连接的创建和销毁开销较大，且并发数受系统资源限制，会造成性能瓶颈。RabbitMQ使用信道的方式来传输数据。信道是建立在真实的TCP连接内的虚拟连接，且每条TCP连接上的信道数量没有限制。

### 如何避免消息重复投递或重复消费？

​		在消息生产时，MQ内部针对每条生产者发送的消息生成一个`inner-msg-id`，作为去重和幂等的依据（消息投递失败并重传），避免重复的消息进入队列；在消息消费时，要求消息体中必须要有一个`bizId`（对于同一业务全局唯一，如支付ID、订单ID、帖子ID等）作为去重和幂等的依据，避免同一条消息被重复消费。

这个问题针对业务场景来答分以下几点：

1.比如，你拿到这个消息做数据库的insert操作。那就容易了，给这个消息做一个唯一主键，那么就算出现重复消费的情况，就会导致主键冲突，避免数据库出现脏数据。

2.再比如，你拿到这个消息做redis的set的操作，那就容易了，不用解决，因为你无论set几次结果都是一样的，set操作本来就算幂等操作。

3.如果上面两种情况还不行，上大招。准备一个第三方介质,来做消费记录。以redis为例，给消息分配一个全局id，只要消费过该消息，将<id,message>以K-V形式写入redis。那消费者开始消费前，先去redis中查询有没消费记录即可。

### **消息传输保证层级？**

![image-20220416214126472](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220416214126472.png)



# RabbitMQ简单案例

### Helloworld

​			生产者：

```java
public class Producer {
    //队列名称
    public static final String QUEUE_NAME = "hello";
    //发消息
    public static void main(String[] args) throws Exception {
        //创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //工厂IP 连接RabbitMQ的队列
        factory.setHost("127.0.0.1");
        //用户名和密码
        factory.setUsername("guest");
        factory.setPassword("guest");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();
        /**
         * 生成一个队列
         * 1.队列名称
         * 2.队列里面的消息是否持久化 默认消息存储在内存中
         * 3.该队列是否只供一个消费者进行消费 是否进行共享  为true则可以多个消费者消费
         * 4.是否自动删除 最后一个消费者断开连接以后 该队列是否自动删除 true 自动删除
         * 5.其他参数
         */
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        String msg = "hello world";
        /**
         * 发送一个消息
         * 1.发送到那个交换机
         * 2.路由的 key 是哪个
         * 3.其他的参数信息
         * 4.发送消息的消息体
         */
        channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());
        System.out.println("消息发送完毕");
    }
```

消费者：

```java
public class Consumer {
    //队列名称
    public static final String QUEUE_NAME = "hello";
    //接收
    public static void main(String[] args) throws Exception {
        //创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //工厂IP 连接RabbitMQ的队列
        factory.setHost("127.0.0.1");
        //用户名和密码
        factory.setUsername("guest");
        factory.setPassword("guest");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();
        System.out.println("等待接收消息");
        //推送的消息如何进行消费的接口回调
        DeliverCallback deliverCallback = (consumerTag,message)->{
            String msg = new String(message.getBody());
            System.out.println(msg);
        };

        //取消消费的一个回调接口 如在消费的时候队列被删除掉了
        CancelCallback cancelCallback= consumerTag->{
                System.out.println("消息消费被中断");
        };
        /**
         * 消费者消费消息
         * 1.消费哪个队列
         * 2.true 接收到传递过来的消息后acknowledged（应答服务器），false 接收到消息后不应答服务器
         * 3.deliverCallback： 当一个消息发送过来后的回调接口
         * 4.cancelCallback：当一个消费者取消订阅时的回调接口;取消消费者订阅队列时除了使用{@link Channel#basicCancel}之外的所有方式都会调用该回调方法
         */
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
        System.out.println("消息消费完毕");
    }
```

### Work Queues（工作队列模式)

- 工作队列模式为一个生产者对应多个消费者，但是只有一个消费者获得消息，即一个队列被多个消费者监听，但一条消息只能被其中的一个消费者获取。并且**消息是轮询的**

- 它的主要作用就是规避了实时的执行资源密集型任务（ resource-intensive task），因为这会造成响应时间过长，影响用户体验。Work Queues通过把一些实时性不强的任务存储到消息队列中，然后后台的工作者（worker）会在特定的情况下完成这些任务。

  　　举个例子来说，用户注册是一个资源密集型的任务，因为它需要经过存储用户基本信息（用户名，邮箱，密码），发送邮箱验证码、或者更有甚者，存入注册日志（操作日志）等步骤。传统的串行做法如下所示。

  ![image-20220414113828393](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414113828393.png)

  可以看到，在用户填写完注册信息并点击提交以后，需要经历3个步骤，其中第一个步骤，判断注册信息是否合法，合法则存入数据库，这是注册的核心步骤，而后面两个步骤并不是十分迫切，无需在这个请求中马上完成。而传统的串行模式一般都是在一个请求中塞满逻辑处理，无论是否迫切的逻辑请求。这样会大大加重一个请求的负担，无论是用户等待时间，程序的压力上，都不是一种好的做法。尤其是对于web应用，我们知道一个web请求是一个短连接，在一个短连接中做过于复杂的逻辑运算操作，显然是不合适的。所以消息分布队列在web应用中尤为有用。

  ![image-20220414113902016](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414113902016.png)

  我们将上述串行的的方式改为用消息队列的形式来实现，可以看到此时我把一个请求里面做的事情分解到三个请求来实现，这样每个请求的时间都降低了，特别对于用户而言，他的等待时间大大减少，而这样也可以充分利用了cpu的性能。

#### 具体案例

##### **消费者**

```java
public class Worker01 {
    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        DeliverCallback deliverCallback = (comsumerTag,message)->{
            System.out.println("接收到的消息："+new String(message.getBody()));
        };
        CancelCallback cancelCallback = (comsumerTag)->{
            System.out.println("消息者取消消费接口回调逻辑");
        };
        System.out.println("C2等待接收消息.....");
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}
```

**设置可以多线程**

![image-20220414115251639](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414115251639.png)

两个线程：

![image-20220414115330294](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414115330294.png)

##### 生产者

```java
public class Producer01 {
    public static final String QUEUE_NAME = "hello";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitUtils.getChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //从控制台当中接收消息
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());
            System.out.println("发送消息完成："+msg);
        }
    }
}
```

##### 运行结果

![image-20220414120853985](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414120853985.png)

![image-20220414120906768](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414120906768.png)

![image-20220414120915688](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20220414120915688.png)

具体案例



# 部分RabbitMQ学习文章和项目

[Rabbit入门案例](https://developer.aliyun.com/article/769883)以及对应的   [项目代码](https://github.com/yehongzhi/mall)

[RabbitMQ常问面试题](https://zhangc233.github.io/2021/07/30/RabbitMQ%E9%9D%A2%E8%AF%95%E9%A2%98/)

