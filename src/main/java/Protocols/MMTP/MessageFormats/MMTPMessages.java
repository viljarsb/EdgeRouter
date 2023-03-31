// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: MMTPMessages.proto

package Protocols.MMTP.MessageFormats;

public final class MMTPMessages {
  private MMTPMessages() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ProtocolMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ProtocolMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Register_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Register_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Unregister_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Unregister_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_DirectApplicationMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DirectApplicationMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_SubjectCastApplicationMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SubjectCastApplicationMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RoutingUpdate_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RoutingUpdate_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\022MMTPMessages.proto\032\037google/protobuf/ti" +
      "mestamp.proto\">\n\017ProtocolMessage\022\032\n\004type" +
      "\030\001 \001(\0162\014.MessageType\022\017\n\007content\030\002 \001(\014\"Y\n" +
      "\010Register\022\021\n\tinterests\030\001 \003(\t\022!\n\024want_dir" +
      "ect_messages\030\002 \001(\010H\000\210\001\001B\027\n\025_want_direct_" +
      "messages\"[\n\nUnregister\022\021\n\tinterests\030\001 \003(" +
      "\t\022!\n\024want_direct_messages\030\002 \001(\010H\000\210\001\001B\027\n\025" +
      "_want_direct_messages\"\210\001\n\030DirectApplicat" +
      "ionMessage\022\n\n\002id\030\001 \001(\t\022\022\n\nrecipients\030\002 \003" +
      "(\t\022\016\n\006sender\030\003 \001(\t\022+\n\007expires\030\004 \001(\0132\032.go" +
      "ogle.protobuf.Timestamp\022\017\n\007payload\030\005 \001(\014" +
      "\"\212\001\n\035SubjectCastApplicationMessage\022\n\n\002id" +
      "\030\001 \001(\t\022\017\n\007subject\030\002 \001(\t\022\016\n\006sender\030\003 \001(\t\022" +
      "+\n\007expires\030\004 \001(\0132\032.google.protobuf.Times" +
      "tamp\022\017\n\007payload\030\005 \001(\014\"/\n\rRoutingUpdate\022\014" +
      "\n\004MRNs\030\001 \003(\t\022\020\n\010subjects\030\002 \003(\t*\205\001\n\013Messa" +
      "geType\022\014\n\010REGISTER\020\000\022\016\n\nUNREGISTER\020\001\022\036\n\032" +
      "DIRECT_APPLICATION_MESSAGE\020\002\022$\n SUBJECT_" +
      "CAST_APPLICATION_MESSAGE\020\003\022\022\n\016ROUTING_UP" +
      "DATE\020\004B\032\n\010MMS.MMTPB\014MMTPMessagesP\001b\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
        });
    internal_static_ProtocolMessage_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ProtocolMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ProtocolMessage_descriptor,
        new String[] { "Type", "Content", });
    internal_static_Register_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_Register_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Register_descriptor,
        new String[] { "Interests", "WantDirectMessages", "WantDirectMessages", });
    internal_static_Unregister_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_Unregister_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Unregister_descriptor,
        new String[] { "Interests", "WantDirectMessages", "WantDirectMessages", });
    internal_static_DirectApplicationMessage_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_DirectApplicationMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_DirectApplicationMessage_descriptor,
        new String[] { "Id", "Recipients", "Sender", "Expires", "Payload", });
    internal_static_SubjectCastApplicationMessage_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_SubjectCastApplicationMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_SubjectCastApplicationMessage_descriptor,
        new String[] { "Id", "Subject", "Sender", "Expires", "Payload", });
    internal_static_RoutingUpdate_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_RoutingUpdate_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RoutingUpdate_descriptor,
        new String[] { "MRNs", "Subjects", });
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}