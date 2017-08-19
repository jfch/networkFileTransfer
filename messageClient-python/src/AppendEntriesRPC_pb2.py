# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: AppendEntriesRPC.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='AppendEntriesRPC.proto',
  package='',
  serialized_pb=_b('\n\x16\x41ppendEntriesRPC.proto\"*\n\x08ImageMsg\x12\x0b\n\x03key\x18\x01 \x02(\t\x12\x11\n\timageData\x18\x02 \x02(\x0c\"\xc7\x01\n\rAppendEntries\x12\x10\n\x08leaderId\x18\x01 \x02(\x05\x12\x1b\n\x08imageMsg\x18\x02 \x02(\x0b\x32\t.ImageMsg\x12\x1f\n\x17timeStampOnLatestUpdate\x18\x03 \x02(\x03\x12/\n\x0brequestType\x18\x04 \x02(\x0e\x32\x1a.AppendEntries.RequestType\"5\n\x0bRequestType\x12\x07\n\x03GET\x10\x00\x12\x08\n\x04POST\x10\x01\x12\x07\n\x03PUT\x10\x02\x12\n\n\x06\x44\x45LETE\x10\x03\"j\n\x15\x41ppendEntriesResponse\x12\x33\n\tisUpdated\x18\x01 \x02(\x0e\x32 .AppendEntriesResponse.IsUpdated\"\x1c\n\tIsUpdated\x12\x07\n\x03YES\x10\x00\x12\x06\n\x02NO\x10\x01\"\x99\x01\n\x13\x41ppendEntriesPacket\x12\x15\n\runixTimeStamp\x18\x01 \x02(\x03\x12\'\n\rappendEntries\x18\x02 \x01(\x0b\x32\x0e.AppendEntriesH\x00\x12\x37\n\x15\x61ppendEntriesResponse\x18\x03 \x01(\x0b\x32\x16.AppendEntriesResponseH\x00\x42\t\n\x07payloadB\x02H\x01')
)
_sym_db.RegisterFileDescriptor(DESCRIPTOR)



_APPENDENTRIES_REQUESTTYPE = _descriptor.EnumDescriptor(
  name='RequestType',
  full_name='AppendEntries.RequestType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='GET', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='POST', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='PUT', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='DELETE', index=3, number=3,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=217,
  serialized_end=270,
)
_sym_db.RegisterEnumDescriptor(_APPENDENTRIES_REQUESTTYPE)

_APPENDENTRIESRESPONSE_ISUPDATED = _descriptor.EnumDescriptor(
  name='IsUpdated',
  full_name='AppendEntriesResponse.IsUpdated',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='YES', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='NO', index=1, number=1,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=350,
  serialized_end=378,
)
_sym_db.RegisterEnumDescriptor(_APPENDENTRIESRESPONSE_ISUPDATED)


_IMAGEMSG = _descriptor.Descriptor(
  name='ImageMsg',
  full_name='ImageMsg',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='key', full_name='ImageMsg.key', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='imageData', full_name='ImageMsg.imageData', index=1,
      number=2, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=26,
  serialized_end=68,
)


_APPENDENTRIES = _descriptor.Descriptor(
  name='AppendEntries',
  full_name='AppendEntries',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='leaderId', full_name='AppendEntries.leaderId', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='imageMsg', full_name='AppendEntries.imageMsg', index=1,
      number=2, type=11, cpp_type=10, label=2,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='timeStampOnLatestUpdate', full_name='AppendEntries.timeStampOnLatestUpdate', index=2,
      number=3, type=3, cpp_type=2, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='requestType', full_name='AppendEntries.requestType', index=3,
      number=4, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _APPENDENTRIES_REQUESTTYPE,
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=71,
  serialized_end=270,
)


_APPENDENTRIESRESPONSE = _descriptor.Descriptor(
  name='AppendEntriesResponse',
  full_name='AppendEntriesResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='isUpdated', full_name='AppendEntriesResponse.isUpdated', index=0,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _APPENDENTRIESRESPONSE_ISUPDATED,
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=272,
  serialized_end=378,
)


_APPENDENTRIESPACKET = _descriptor.Descriptor(
  name='AppendEntriesPacket',
  full_name='AppendEntriesPacket',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='unixTimeStamp', full_name='AppendEntriesPacket.unixTimeStamp', index=0,
      number=1, type=3, cpp_type=2, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='appendEntries', full_name='AppendEntriesPacket.appendEntries', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='appendEntriesResponse', full_name='AppendEntriesPacket.appendEntriesResponse', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='payload', full_name='AppendEntriesPacket.payload',
      index=0, containing_type=None, fields=[]),
  ],
  serialized_start=381,
  serialized_end=534,
)

_APPENDENTRIES.fields_by_name['imageMsg'].message_type = _IMAGEMSG
_APPENDENTRIES.fields_by_name['requestType'].enum_type = _APPENDENTRIES_REQUESTTYPE
_APPENDENTRIES_REQUESTTYPE.containing_type = _APPENDENTRIES
_APPENDENTRIESRESPONSE.fields_by_name['isUpdated'].enum_type = _APPENDENTRIESRESPONSE_ISUPDATED
_APPENDENTRIESRESPONSE_ISUPDATED.containing_type = _APPENDENTRIESRESPONSE
_APPENDENTRIESPACKET.fields_by_name['appendEntries'].message_type = _APPENDENTRIES
_APPENDENTRIESPACKET.fields_by_name['appendEntriesResponse'].message_type = _APPENDENTRIESRESPONSE
_APPENDENTRIESPACKET.oneofs_by_name['payload'].fields.append(
  _APPENDENTRIESPACKET.fields_by_name['appendEntries'])
_APPENDENTRIESPACKET.fields_by_name['appendEntries'].containing_oneof = _APPENDENTRIESPACKET.oneofs_by_name['payload']
_APPENDENTRIESPACKET.oneofs_by_name['payload'].fields.append(
  _APPENDENTRIESPACKET.fields_by_name['appendEntriesResponse'])
_APPENDENTRIESPACKET.fields_by_name['appendEntriesResponse'].containing_oneof = _APPENDENTRIESPACKET.oneofs_by_name['payload']
DESCRIPTOR.message_types_by_name['ImageMsg'] = _IMAGEMSG
DESCRIPTOR.message_types_by_name['AppendEntries'] = _APPENDENTRIES
DESCRIPTOR.message_types_by_name['AppendEntriesResponse'] = _APPENDENTRIESRESPONSE
DESCRIPTOR.message_types_by_name['AppendEntriesPacket'] = _APPENDENTRIESPACKET

ImageMsg = _reflection.GeneratedProtocolMessageType('ImageMsg', (_message.Message,), dict(
  DESCRIPTOR = _IMAGEMSG,
  __module__ = 'AppendEntriesRPC_pb2'
  # @@protoc_insertion_point(class_scope:ImageMsg)
  ))
_sym_db.RegisterMessage(ImageMsg)

AppendEntries = _reflection.GeneratedProtocolMessageType('AppendEntries', (_message.Message,), dict(
  DESCRIPTOR = _APPENDENTRIES,
  __module__ = 'AppendEntriesRPC_pb2'
  # @@protoc_insertion_point(class_scope:AppendEntries)
  ))
_sym_db.RegisterMessage(AppendEntries)

AppendEntriesResponse = _reflection.GeneratedProtocolMessageType('AppendEntriesResponse', (_message.Message,), dict(
  DESCRIPTOR = _APPENDENTRIESRESPONSE,
  __module__ = 'AppendEntriesRPC_pb2'
  # @@protoc_insertion_point(class_scope:AppendEntriesResponse)
  ))
_sym_db.RegisterMessage(AppendEntriesResponse)

AppendEntriesPacket = _reflection.GeneratedProtocolMessageType('AppendEntriesPacket', (_message.Message,), dict(
  DESCRIPTOR = _APPENDENTRIESPACKET,
  __module__ = 'AppendEntriesRPC_pb2'
  # @@protoc_insertion_point(class_scope:AppendEntriesPacket)
  ))
_sym_db.RegisterMessage(AppendEntriesPacket)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('H\001'))
# @@protoc_insertion_point(module_scope)
