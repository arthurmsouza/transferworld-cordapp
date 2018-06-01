# TransferWorld CorDapp

**NOTE:** This CorDapp targets Corda v2.0.

Send money to all your friends running Corda nodes!

## Pre-Requisites

You will need the following installed on your machine before you can start:

* Latest [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  installed and available on your path
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) minimum version 2017.1
* git

## Getting Set Up

To get started, clone this repository with:

     git clone https://github.com/arthurmsouza/transferworld-cordapp.git

And change directories to the newly cloned repo:

     cd transferworld-cordapp

## Building the TransferWorld CorDapp:

**Unix:** 

     ./gradlew clean deployNodes

**Windows:**

     gradlew.bat clean deployNodes

## Running the Nodes:

Once the build finishes, change directories to the folder where the newly
built nodes are located:

**Kotlin:**

     cd build/nodes

**Unix:**

     ./runnodes

**Windows:**

    runnodes.bat

You should now have three Corda nodes running on your machine serving
the TransferWorld CorDapp.

Five windows will open in the terminal. One for each node's node shell, plus webservers for Node A and Node B.

## Interacting with the CorDapp via HTTP

The TransferWorld CorDapp defines a couple of HTTP API end-points.

The nodes can be found using the following port numbers output in the web server
terminal window or in the `build.gradle` file.

     NodeA: localhost:10007
     NodeB: localhost:10010

Sending amount of money from Node A to Node B 

    http://localhost:10007/api/transferworld/transfer?target=NodeB&amount=100

Showing all of Node B's amounts:

     http://localhost:10010/api/transferworld/amounts
     
Finding out who Node B is:

    http://localhost:10010/api/transferworld/me

Finding out who Node B can send amount of money to:

    http://localhost:10010/api/transferworld/peers

## Using the RPC Client

Use the gradle command (for Node A):

     ./gradlew runTransferWorldRPCNodeA
     
or (for Node B):
     
     ./gradlew runTransferWorldRPCNodeB

When running it should enumerate all previously received amount of money as well as show any new amount
received when they are sent to you.

## Using the node shell

The node shell is a great way to test your CorDapps without having to create a user interface. 

When the nodes are up and running, use the following command to send a amount of money to another node:

    flow start TransferFlow target: [NODE_NAME]
    
Where `NODE_NAME` is 'NodeA' or 'NodeB'. The space after the `:` is required. As with the web API, you are not
required to use the full X500 name in the node shell. Note you can't sent a amount of money to yourself because that's not cool!

To see all the amounts of money in your vault:

    run vaultQuery contractStateType: net.corda.transferworld.state.AmountState

##Test IDEA issues

Fiber classes not instrumented
If you run JUnit tests that use flows then IntelliJ will use a default JVM command line of just

    -ea, which is insufficient.

You will need to open the run config and change it to read

    -ea -javaagent:lib/quasar.jar

and make sure the working directory is set to the root of the Corda repository.
Alternatively, make sure you have the quasar.jar file in your application source tree and set the paths appropriately so the JVM can find it.

