import asyncio
import bleak
import logging

#logging.basicConfig(filename='my_log.log', level=logging.INFO)  # Configurar nivel de registro según tus necesidades
logging.basicConfig(level=logging.INFO)  # Configurar nivel de registro según tus necesidades


#result = {}
check = {}

async def notification_callback(sender, value):
    #hex_value = "".join(format(x, "02x") for x in value)
    #global result
    global check
    bs = str(value.hex()).upper()
    params = {
        "voltage": "C0", # V
        "current": "C1", # A
        "dir_of_current": "D1", # binary
        "ah_remaining": "D2", # Ah
        "discharge": "D3",	# KWh
        "charge": "D4",		# KWh	
        "mins_remaining": "D6", #  Min
        "impedance": "D7",           # mΩ
        "power": "D8", # W
        "temp": "D9", # C/F
        "battery_capacity": "B1" # A
    }
    battery_capacity_ah = 360

    params_keys = list(params.keys())
    params_values = list(params.values())

    # split bs into a list of all values and hex keys
    bs_list = [bs[i:i+2] for i in range(0, len(bs), 2)]

    # reverse the list so that values come after hex params
    bs_list_rev = list(reversed(bs_list))

    values = {}

    # iterate through the list and if a param is found,
    # add it as a key to the dict. The value for that key is a
    # concatenation of all following elements in the list
    # until a non-numeric element appears. This would either
    # be the next param or the beginning hex value.
    for i in range(len(bs_list_rev)-1):
        if bs_list_rev[i] in params_values:
            value_str = ''
            j = i + 1
            while j < len(bs_list_rev) and bs_list_rev[j].isdigit():
                value_str = bs_list_rev[j] + value_str
                j += 1

            position = params_values.index(bs_list_rev[i])

            key = params_keys[position]
            values[key] = value_str


    # check if dir_of_current exist if not asign if charging or dischargin exist 
    if "dir_of_current" not in values and "charging" not in check:
        if "discharge" in values and "charge" not in values:
            values["dir_of_current"] = "00"
        elif "charge" in values and "discharge" not in values:
            values["dir_of_current"] = "01"
            
    # now format to the correct decimal place, or perform other formatting            
    for key,value in list(values.items()):
        if not value.isdigit():
            del values[key]

        val_int = int(value)
        if key == "dir_of_current":
            if value == "01":
                check["charging"] = True
            else:
                check["charging"] = False
        elif key == "voltage":
            values[key] = val_int / 100
        elif key == "current":
            values[key] = val_int / 100
        elif key == "discharge":
            values[key] = val_int / 100000
        elif key == "charge":
            values[key] = val_int / 100000                
        elif key == "ah_remaining":
            values[key] = val_int / 1000
        elif key == "mins_remaining":
            values[key] = val_int
        elif key == "impedance":
            values[key] = val_int / 100            
        elif key == "power":
            values[key] = val_int / 100
        elif key == "temp":
            values[key] = val_int - 100
        elif key == "battery_capacity":
            values[key] = val_int / 10           

    # Calculate percentage
    if isinstance(battery_capacity_ah, int) and "ah_remaining" in values:
        soc = values["ah_remaining"] / battery_capacity_ah * 100
        if "soc" not in values or soc != values["soc"]:
            values["soc"] = soc


    # Update old results with new values
    #result.update(values)
    

    # Display current as negative numbers if discharging
    if check["charging"] == False:
        if "current" in values:
            values["current"] *= -1
        if "power" in values:
            values["power"] *= -1


    # Append max capacity
    #if "battery_capacity" not in values:
    #    values["battery_capacity"] = battery_capacity_ah


    logging.info(values)

async def main():
    device_mac = "xxxxxx"
    #target_name_prefix = "BTG"
    read_characteristic_uuid = "0000fff1-0000-1000-8000-00805f9b34fb"
    #send a message to get all the measurement values 
    #send_characteristic_uuid = "0000fff2-0000-1000-8000-00805f9b34fb"
    #message = ":R50=1,2,1,\n"
    #interval_seconds = 60

    while True:  # loop for reestar in error
        try:
            
            #conect with device name not mac
            #devices = await bleak.discover()
            #for device in devices:
            #    if device.name and device.name.startswith(target_name_prefix):
            #        async with bleak.BleakClient(device.address) as client: 
            
            #conect with mac directly          
            async with bleak.BleakClient(device_mac) as client:
                if await client.is_connected():
                    logging.info(f"Connected to device with MAC address {device_mac}")

                    # Configure notifications on the read characteristic
                    await client.start_notify(read_characteristic_uuid, notification_callback)

                    #loop to send the message
                    #while True:
                        # Send the message to the write characteristic
                    #    await client.write_gatt_char(send_characteristic_uuid, message.encode())
                    #    logging.info(f"Sent message to characteristic {send_characteristic_uuid}: {message}")

                        # Wait for the specified interval
                    #    await asyncio.sleep(interval_seconds)

                else:
                    logging.error(f"Could not connect to device with MAC address {device_mac}")

        except bleak.BleakError as e:
            logging.error(f"Error: {e}")
            continue  # continue in error case 

if __name__ == "__main__":
    asyncio.run(main())
