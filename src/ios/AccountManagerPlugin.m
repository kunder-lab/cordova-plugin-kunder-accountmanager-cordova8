/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

#import "AccountManagerPlugin.h"
#include <sys/types.h>
#include <sys/sysctl.h>
#import <Cordova/CDV.h>

@implementation AccountManagerPlugin
- (void) initWithKey:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void) addAccount:(CDVInvokedUrlCommand*)command{
    
   
    NSString *userAccount = (NSString*)[command.arguments objectAtIndex:0];
    NSString *password = (NSString*)[command.arguments objectAtIndex:1];
    NSString *service = (NSString*)[command.arguments objectAtIndex:2];
    NSString *group = (NSString*)[command.arguments objectAtIndex:3];
    
    NSMutableDictionary *userData = (NSMutableDictionary*)[command.arguments objectAtIndex:4];

    @try{
        /*Intentando guardar el usuario y contraseña en el keychain*/
        self.MyKeychainWrapper = [[KeychainWrapper alloc]initWithService:service withGroup:group withKey:@"userAccount"];
        if(![self.MyKeychainWrapper insertData:userAccount])
            [self.MyKeychainWrapper updateData:userAccount];
                
        self.MyKeychainWrapper = [[KeychainWrapper alloc]initWithService:service withGroup:group withKey:@"password"];
        if(![self.MyKeychainWrapper insertData:password])
            [self.MyKeychainWrapper updateData:password];
                
        /*Reccorriendo diccionario y guardando la información en el keychain*/
        for(NSString *key in userData){
            self.MyKeychainWrapper = [[KeychainWrapper alloc]initWithService:service withGroup:group withKey:key];
            if(![self.MyKeychainWrapper insertData:[userData objectForKey:key]]){
                [self.MyKeychainWrapper updateData:[userData objectForKey:key]];
            }
        }
        
            
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    @catch(NSException *exception){
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"No se puede guardar el dato en el keychain"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
        
    
    
    
}

- (void) removeAccount:(CDVInvokedUrlCommand*)command{
    //Implica eliminar todo el keychain
    
    self.MyKeychainWrapper = [[KeychainWrapper alloc]init];
    if([self.MyKeychainWrapper removeAllData]){
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"Error al intentar eliminar los datos del keychain"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) getUserAccount:(CDVInvokedUrlCommand*)command{
    NSString *service = (NSString*)[command.arguments objectAtIndex:0];
    NSString *group = (NSString*)[command.arguments objectAtIndex:1];
    NSString *returnKey = (NSString*)[command.arguments objectAtIndex:2];
    
    self.MyKeychainWrapper = [[KeychainWrapper alloc] initWithService:service withGroup:group withKey:@"userAccount"];
    NSData *resultData = [self.MyKeychainWrapper getData];
    
    if(resultData != nil){
        NSString *responseData = [[NSString alloc] initWithData:resultData encoding: NSUTF8StringEncoding];
        NSMutableDictionary* retorno = [NSMutableDictionary dictionaryWithCapacity:1];
        [retorno setObject:responseData forKey:returnKey];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:retorno];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else{
        NSString *message = @"Error al obtener el valor userAccount del keychain";
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) getPassword:(CDVInvokedUrlCommand*)command{
    /*Es necesario el servicio y el group id, ya que la clave es constante*/
    NSString *service = (NSString*)[command.arguments objectAtIndex:0];
    NSString *group = (NSString*)[command.arguments objectAtIndex:1];
    //Este parámetro es necesario para retornar el valor con una key personalizada
    NSString *returnKey = (NSString*)[command.arguments objectAtIndex:2];
    
    self.MyKeychainWrapper = [[KeychainWrapper alloc]initWithService:service withGroup:group withKey:@"password"];
    NSData *passwordData = [self.MyKeychainWrapper getData];
    
    if(passwordData != nil){
        NSString *password = [[NSString alloc] initWithData:passwordData encoding:NSUTF8StringEncoding];
        NSMutableDictionary* retorno = [NSMutableDictionary dictionaryWithCapacity:1];
        [retorno setObject:password forKey:returnKey];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:retorno];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else{
        NSString *message = @"Error al obtener el password del keychain";
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) getDataFromKey:(CDVInvokedUrlCommand*)command{
    NSString *service = (NSString*)[command.arguments objectAtIndex:0];
    NSString *group = (NSString*)[command.arguments objectAtIndex:1];
    NSString *key = (NSString*)[command.arguments objectAtIndex:2];
    
    self.MyKeychainWrapper = [[KeychainWrapper alloc] initWithService:service withGroup:group withKey:key];
    NSData *resultData = [self.MyKeychainWrapper getData];
    
    if(resultData != nil){
        NSString *responseData = [[NSString alloc] initWithData:resultData encoding: NSUTF8StringEncoding];
        NSMutableDictionary* retorno = [NSMutableDictionary dictionaryWithCapacity:1];
        [retorno setObject:responseData forKey:key];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:retorno];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else{
        NSString *message = @"Error al obtener el valor del keychain";
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) setUserData:(CDVInvokedUrlCommand*)command{
    NSString *service = (NSString*)[command.arguments objectAtIndex:0];
    NSString *group = (NSString*)[command.arguments objectAtIndex:1];
    NSMutableDictionary *userData = (NSMutableDictionary*)[command.arguments objectAtIndex:2];
    
    @try{
        
        /*Reccorriendo diccionario y guardando la información en el keychain*/
        for(NSString *key in userData){
            self.MyKeychainWrapper = [[KeychainWrapper alloc]initWithService:service withGroup:group withKey:key];
            if(![self.MyKeychainWrapper updateData:[userData objectForKey:key]]){
                //Si retorna NO, implica que no existe, por lo tanto se añade
                [self.MyKeychainWrapper insertData:[userData objectForKey:key]];
            }

        }
        
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    @catch(NSException *exception){
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"No se puede actualizar los datos en el keychain"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) changePassword:(CDVInvokedUrlCommand*)command{
    //Este método implica actualizar la contraseña
    NSString *service = (NSString*)[command.arguments objectAtIndex:0];
    NSString *group = (NSString*)[command.arguments objectAtIndex:1];
    NSString *newPassword = (NSString*)[command.arguments objectAtIndex:2];
    
    @try{
        self.MyKeychainWrapper = [[KeychainWrapper alloc] initWithService:service withGroup:group withKey:@"password"];
        
        if([self.MyKeychainWrapper updateData:newPassword]){
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        else{
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"No se puede actualizar el password en el keychain"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }
    @catch(NSException *exception){
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"No se puede actualizar el password en el keychain"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

@end
